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
import org.hibernate.validator.internal.xml.mapping.MappingXmlParser;
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
    private final int MAX_RETRIES = 2;
    private final int SLEEP_TIME = 300;

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
        int totalSuccess = 0;

        // 누적 실패(최종 보고용)
        List<PushSender.TokenFailure> finalFailures = new ArrayList<>();

        // 이번 라운드에 보낼 대상
        List<String> pending = tokens;

        for (int attemptNo = 0; attemptNo <= MAX_RETRIES; attemptNo++) {
            if (pending.isEmpty()) break;

            //첫 번째는 바로 가고 두번째부터 대기 시간 가진다.
            if (attemptNo > 0) {
                sleepBackoff(SLEEP_TIME);
            }

            SendAttempt attempt = attempt(pending, title, body);
            totalSuccess += attempt.successCount;

            //실패한 것들 조회
            List<TokenFailure> failures = attempt.failures();

            //영구적인 실패는 바로 처리
            List<PushSender.TokenFailure> permanent = failures.stream()
                    .filter(f -> isPermanent(f.errorCode()))
                    .toList();

            if (!permanent.isEmpty()) {
                deactivateTokens(permanent.stream().map(TokenFailure::token).toList());
                finalFailures.addAll(permanent);
            }

            //다시 시도할 토큰들 정리
            List<PushSender.TokenFailure> retryable = failures.stream()
                    .filter(f -> isRetryableError(f.errorCode()))
                    .toList();

            //둘 다 아닌 것들은 재시도 안함
            List<PushSender.TokenFailure> other = failures.stream()
                    .filter(f -> !isPermanent(f.errorCode()) && !isRetryableError(f.errorCode()))
                    .toList();
            finalFailures.addAll(other);

            //다음 시도에 할 토큰들 정리
            pending = retryable.stream().map(TokenFailure::token).toList();

            //마지막 시도라면: 그냥 다 실패로 처리
            if (attemptNo == MAX_RETRIES && !pending.isEmpty()) {
                finalFailures.addAll(retryable);
            }
        }

        return new MulticastResult(totalSuccess, finalFailures.size(), finalFailures);
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
            //에러가 발생하면 에러를 담아서 반환한다. 다음 시도 때 같이 할 수 있도록
            String code = (e.getErrorCode() != null) ? e.getErrorCode().toString() : "INTERNAL";
            List<TokenFailure> failures = tokens.stream().map(t -> new TokenFailure(t, code, e.getMessage())).toList();
            return new SendAttempt(0, failures);
        }
    }

    private boolean isRetryableError(String code) {
        return code.equals("UNAVAILABLE") || code.equals("INTERNAL")|| code.equals("DEADLINE_EXCEEDED")|| code.equals("INVALID_ARGUMENT");
    }

    //재시도가 불가능한 토큰들
    private boolean isPermanent(String code) {
        return code.equals("UNREGISTERED") || code.equals("INVALID_ARGUMENT") ||code.equals("SENDER_ID_MISMATCH") || code.equals("RESOURCE_EXHAUSTED");
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

