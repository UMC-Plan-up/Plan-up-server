package com.planup.planup.domain.notification.entity.device;

import java.util.Collection;
import java.util.Map;

public interface PushSender {

    /** 단일 토큰에 알림+데이터 발송 */
    String sendToToken(String token, String title, String body, Map<String, String> data) throws Exception;

    /** 데이터 전용 */
    String sendDataOnly(String token, Map<String, String> data) throws Exception;

    /** 토픽 브로드캐스트 */
    String sendToTopic(String topic, String title, String body) throws Exception;

    /** 여러 토큰에 멀티캐스트 */
    MulticastResult sendMulticast(Collection<String> tokens, String title, String body) throws Exception;

    /** 멀티캐스트 결과를 담는 도메인 타입 */
    record MulticastResult(int successCount, int failureCount, java.util.List<TokenFailure> failures) {}
    record TokenFailure(String token, String errorCode, String message) {}
}
