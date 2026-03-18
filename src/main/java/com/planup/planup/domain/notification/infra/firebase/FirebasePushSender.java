package com.planup.planup.domain.notification.infra.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.planup.planup.domain.notification.entity.device.PushSender;
import com.planup.planup.domain.notification.service.deviceToken.DeviceTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.google.firebase.messaging.Notification;

import java.util.*;


/**
 * fireBase 데이터베이스와 실제로 연결되는 부분
 * 여기서 사용 중인 Notification은 우리가 프로젝트 안에서 정의한 Notification 아님. fireBase 내부적으로 사용되는 클래스
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FirebasePushSender implements PushSender{

    private final DeviceTokenService deviceTokenService;
    private static final int MAX_RETRIES = 2;
    private static final int SLEEP_TIME = 300;

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
        var msg = Message.builder().setToken(token).putAllData(data == null ? Map.of() : data).build();
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
    public MulticastResult sendMulticast(Collection<String> tokensCollection, String title, String body, Map<String, String> data) {
        int totalSuccess = 0;

        // 누적 실패(최종 보고용)
        List<PushSender.TokenFailure> finalFailures = new ArrayList<>();

        int requestedTokenCount = tokensCollection == null ? 0 : tokensCollection.size();
        log.info("FCM multicast requested. requestedTokenCount={}, title={}, bodyLength={}, dataKeys={}",
                requestedTokenCount,
                title,
                body == null ? 0 : body.length(),
                data == null ? List.of() : data.keySet());

        if (tokensCollection == null || tokensCollection.isEmpty()) {
            log.info("FCM multicast skipped. reason=empty_tokens");
            return new MulticastResult(0, 0, List.of());
        }

        List<String> tokens = tokensCollection.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        log.info("FCM multicast normalized. distinctTokenCount={}", tokens.size());

        if (tokens.isEmpty()) {
            log.info("FCM multicast skipped. reason=no_valid_tokens");
            return new MulticastResult(0, 0, List.of());
        }


        // 이번 라운드에 보낼 대상
        List<String> pending = tokens;

        for (int attemptNo = 0; attemptNo <= MAX_RETRIES; attemptNo++) {
            if (pending.isEmpty()) {
                log.info("FCM multicast loop ended early. reason=no_pending_tokens, attemptNo={}", attemptNo);
                break;
            }

            //첫 번째는 바로 가고 두번째부터 대기 시간 가진다.
            if (attemptNo > 0) {
                log.info("FCM multicast retry wait. attemptNo={}, sleepMs={}", attemptNo, SLEEP_TIME);
                sleepBackoff(SLEEP_TIME);
            }

            log.info("FCM multicast attempt started. attemptNo={}, pendingCount={}, pendingTokens={}",
                    attemptNo, pending.size(), maskTokens(pending));


            SendAttempt attempt = attempt(pending, title, body, data);
            totalSuccess += attempt.successCount();

            List<TokenFailure> failures = attempt.failures();

            List<PushSender.TokenFailure> permanent = failures.stream()
                    .filter(f -> isPermanent(f.errorCode()))
                    .toList();

            List<PushSender.TokenFailure> retryable = failures.stream()
                    .filter(f -> isRetryableError(f.errorCode()))
                    .toList();

            List<PushSender.TokenFailure> other = failures.stream()
                    .filter(f -> !isPermanent(f.errorCode()) && !isRetryableError(f.errorCode()))
                    .toList();

            log.info("FCM multicast attempt finished. attemptNo={}, successCount={}, failureCount={}, permanentCount={}, retryableCount={}, otherCount={}",
                    attemptNo,
                    attempt.successCount(),
                    failures.size(),
                    permanent.size(),
                    retryable.size(),
                    other.size());

            if (!permanent.isEmpty()) {
                log.warn("FCM permanent token failures. attemptNo={}, tokens={}, errorCodes={}",
                        attemptNo,
                        maskTokens(permanent.stream().map(TokenFailure::token).toList()),
                        permanent.stream().map(TokenFailure::errorCode).distinct().toList());

                deactivateTokens(permanent.stream().map(TokenFailure::token).toList());
                finalFailures.addAll(permanent);
            }

            if (!other.isEmpty()) {
                log.warn("FCM non-retryable token failures. attemptNo={}, tokens={}, errorCodes={}",
                        attemptNo,
                        maskTokens(other.stream().map(TokenFailure::token).toList()),
                        other.stream().map(TokenFailure::errorCode).distinct().toList());
                finalFailures.addAll(other);
            }

            pending = retryable.stream().map(TokenFailure::token).toList();

            if (!pending.isEmpty()) {
                log.warn("FCM retryable token failures queued for retry. nextAttemptNo={}, retryCount={}, tokens={}, errorCodes={}",
                        attemptNo + 1,
                        pending.size(),
                        maskTokens(pending),
                        retryable.stream().map(TokenFailure::errorCode).distinct().toList());
            }

            if (attemptNo == MAX_RETRIES && !pending.isEmpty()) {
                log.error("FCM retries exhausted. retryableFailuresPromotedToFinal. retryCount={}, tokens={}",
                        pending.size(),
                        maskTokens(pending));
                finalFailures.addAll(retryable);
            }
        }

        List<String> finalFailureTokens = finalFailures.stream().map(TokenFailure::token).toList();
        if (!finalFailureTokens.isEmpty()) {
            log.warn("FCM final failures deactivated. count={}, tokens={}",
                    finalFailureTokens.size(),
                    maskTokens(finalFailureTokens));
            deactivateTokens(finalFailureTokens);
        }

        log.info("FCM multicast completed. totalSuccess={}, totalFailure={}, requestedTokenCount={}",
                totalSuccess, finalFailures.size(), requestedTokenCount);

        return new MulticastResult(totalSuccess, finalFailures.size(), finalFailures);
    }

    //전송을 시도한다.
    private SendAttempt attempt(List<String> tokens, String title, String body, Map<String, String> data) {
        try {
            log.debug("Calling Firebase sendMulticast. tokenCount={}, tokens={}, title={}, bodyLength={}, data={}",
                    tokens.size(),
                    maskTokens(tokens),
                    title,
                    body == null ? 0 : body.length(),
                    data == null ? Map.of() : data);

            var message = MulticastMessage.builder()
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .addAllTokens(tokens)
                    .putAllData(data == null ? Map.of() : data)
                    .build();

            var res = FirebaseMessaging.getInstance().sendMulticast(message);

            var failures = new ArrayList<PushSender.TokenFailure>();
            var responses = res.getResponses();
            int i = 0;

            for (var r : responses) {
                if (!r.isSuccessful()) {
                    var ex = r.getException();
                    String code = ex.getErrorCode() != null ? ex.getErrorCode().toString() : "UNKNOWN";

                    log.warn("FCM token send failed. token={}, errorCode={}, message={}",
                            maskToken(tokens.get(i)),
                            code,
                            ex.getMessage());

                    failures.add(new PushSender.TokenFailure(
                            tokens.get(i),
                            code,
                            ex.getMessage()
                    ));
                } else {
                    log.debug("FCM token send success. token={}, messageId={}",
                            maskToken(tokens.get(i)),
                            r.getMessageId());
                }
                i++;
            }

            log.info("Firebase sendMulticast response received. requestedCount={}, successCount={}, failureCount={}",
                    tokens.size(),
                    res.getSuccessCount(),
                    res.getFailureCount());

            return new SendAttempt(res.getSuccessCount(), failures);

        } catch (FirebaseMessagingException e) {
            String code = (e.getErrorCode() != null) ? e.getErrorCode().toString() : "INTERNAL";

            log.error("Firebase sendMulticast exception. tokenCount={}, tokens={}, errorCode={}, message={}",
                    tokens.size(),
                    maskTokens(tokens),
                    code,
                    e.getMessage(),
                    e);

            List<TokenFailure> failures = tokens.stream()
                    .map(t -> new TokenFailure(t, code, e.getMessage()))
                    .toList();

            return new SendAttempt(0, failures);
        }
    }

    private boolean isRetryableError(String code) {
        return switch (code) {
            case "UNAVAILABLE", "INTERNAL", "DEADLINE_EXCEEDED", "RESOURCE_EXHAUSTED" -> true;
            default -> false;
        };
    }

    private boolean isPermanent(String code) {
        return switch (code) {
            case "UNREGISTERED", "INVALID_ARGUMENT", "SENDER_ID_MISMATCH", "THIRD_PARTY_AUTH_ERROR" -> true;
            default -> false;
        };
    }

    private void deactivateTokens(List<String> tokens) {
        if (tokens == null) return;

        for (String token : tokens) {
            deviceTokenService.deactivateByToken(token);
        }
    }

    private record SendAttempt(int successCount, List<PushSender.TokenFailure> failures) {}

    private void sleepBackoff(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
    }

    private String maskToken(String token) {
        if (token == null) {
            return "null";
        }
        if (token.length() < 12) {
            return token.substring(0, Math.min(4, token.length())) + "...";
        }
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }

    private List<String> maskTokens(Collection<String> tokens) {
        return tokens.stream()
                .map(this::maskToken)
                .toList();
    }
}

