package com.planup.planup.domain.global.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FcmService {

    public String sendToToken(String token, String title, String body, Map<String, String> data) throws FirebaseMessagingException {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setToken(token)
                .setNotification(notification)   // 알림 메시지
                .putAllData(data == null ? Map.of() : data) // 데이터 페이로드
                .build();

        return FirebaseMessaging.getInstance().send(message);
    }

    public String sendDataOnly(String token, Map<String, String> data) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setToken(token)
                .putAllData(data)
                .build();
        return FirebaseMessaging.getInstance().send(message);
    }

    public String sendToTopic(String topic, String title, String body) throws FirebaseMessagingException {
        Message msg = Message.builder()
                .setTopic(topic)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .build();
        return FirebaseMessaging.getInstance().send(msg);
    }

    public BatchResponse sendMulticast(Collection<String> tokens, String title, String body) throws FirebaseMessagingException {
        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .addAllTokens(tokens)
                .build();
        return FirebaseMessaging.getInstance().sendMulticast(message);
    }
}
