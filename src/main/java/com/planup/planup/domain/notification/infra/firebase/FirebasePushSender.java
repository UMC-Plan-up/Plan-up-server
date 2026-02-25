package com.planup.planup.domain.notification.infra.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.PushSendException;
import com.planup.planup.domain.notification.entity.device.PushSender;
import com.planup.planup.domain.notification.service.deviceToken.DeviceTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.google.firebase.messaging.Notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * fireBase 데이터베이스와 실제로 연결되는 부분
 * 여기서 사용 중인 Notification은 우리가 프로젝트 안에서 정의한 Notification 아님. fireBase 내부적으로 사용되는 클래스
 */
@Component
@RequiredArgsConstructor
public class FirebasePushSender implements PushSender{

    private final DeviceTokenService deviceTokenService;

    /**단일 토큰(기기)에 하나의 메시지를 보낸다. (제목/본문을 보낸다) */
    @Override
    public String sendToToken(String token, String title, String body, Map<String, String> data) throws FirebaseMessagingException {
        var notification = Notification.builder().setTitle(title).setBody(body).build();
        var msg = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .putAllData(data == null ? Map.of() : data)
                .build();
        return FirebaseMessaging.getInstance().send(msg);           //전송 부분
    }

    /** 단일 토큰에 데이터만 보냄
     * setNotification 설정 안함 -> 알림이 안갈 수 있음. 조용한 안내 */
    @Override
    public String sendDataOnly(String token, Map<String, String> data) throws FirebaseMessagingException {
        var msg = Message.builder().setToken(token).putAllData(data).build();
        return FirebaseMessaging.getInstance().send(msg);
    }

    /** 특정 토픽을 구독하고 있는 사용자 전체에게 알림을 보낸다.
     * 토픽 구독 설정은 클라이언트가*/
    @Override
    public String sendToTopic(String topic, String title, String body) throws FirebaseMessagingException {
        var msg = Message.builder()
                .setTopic(topic)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .build();
        return FirebaseMessaging.getInstance().send(msg);
    }

    @Override
    public MulticastResult sendMulticast(Collection<String> tokensCollection, String title, String body) {
        ArrayList<String> tokens = new ArrayList<>(tokensCollection);

        //전송 시도 1차
        SendAttempt firstAttempt = attempt(tokens, title, body);

        //다시 시도할 토큰만 추출
        List<String> retryTokens = firstAttempt.failures.stream()
                .filter(f -> isRetryableError(f.errorCode()))
                .map(TokenFailure::token)
                .toList();

        //다시 시도해도 의미 없는 토큰들은 제시도 하지 않고 바로 비활성화
        List<PushSender.TokenFailure> permanentFailure = firstAttempt.failures.stream()
                .filter(f -> isPermanent(f.errorCode()))
                .toList();

        List<String> permanentFailureToken = permanentFailure.stream()
                .map(TokenFailure::token)
                .toList();

        deactivateTokens(permanentFailureToken);

        List<PushSender.TokenFailure> retryFails = List.of();
        int retrySuccess = 0;

        if (!retryTokens.isEmpty()) {
            sleepBackoff(300);
            SendAttempt secondAttempt = attempt(retryTokens, title, body);
            List<TokenFailure> failures = secondAttempt.failures;
            retrySuccess = secondAttempt.successCount;
            retryFails = secondAttempt.failures;

            //재시도에도 불구하고 에러가 난다면 비활성화 처리한다.
            deactivateTokens(failures.stream().map(TokenFailure::token).toList());
        }

        int totalSuccessCnt = firstAttempt.successCount + retrySuccess;
        int totalFailure = permanentFailure.size() + retryFails.size();
        ArrayList<TokenFailure> tokenFailures = new ArrayList<>();
        tokenFailures.addAll(permanentFailure);
        tokenFailures.addAll(retryFails);

        return new MulticastResult(totalSuccessCnt, totalFailure, tokenFailures);
    }

    //전송을 시도한다.
    private SendAttempt attempt(List<String> tokens, String title, String body) {
        try {
            var message = MulticastMessage.builder()
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .addAllTokens(tokens)
                    .build();
            var res = FirebaseMessaging.getInstance().sendMulticast(message);

            var failures = new ArrayList<PushSender.TokenFailure>();
            var responses = res.getResponses();
            int i = 0;
            for (var r : responses) {
                if (!r.isSuccessful()) {
                    var ex = r.getException();
                    String code = ex.getErrorCode().toString();
                    failures.add(new PushSender.TokenFailure(
                            new ArrayList<>(tokens).get(i),
                            code,
                            ex.getMessage()
                    ));
                }
                i++;
            }

            return new SendAttempt(res.getSuccessCount(), failures);
        } catch (FirebaseMessagingException e) {
            throw new PushSendException(ErrorStatus.PushSendError);
        }
    }

    private boolean isRetryableError(String code) {
        return code.equals("UNREGISTERED") || code.equals("INVALID_ARGUMENT");
    }

    //재시도가 불가능한 토큰들
    private boolean isPermanent(String code) {
        return code.equals("UNREGISTERED") || code.equals("INVALID_ARGUMENT");
    }

    private void deactivateTokens(List<String> tokens) {
        for (String token : tokens) {
            deviceTokenService.deactivateByToken(token);
        }
    }

    private record SendAttempt(int successCount, List<PushSender.TokenFailure> failures) {}

    private void sleepBackoff(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignore) {}
    }
}

