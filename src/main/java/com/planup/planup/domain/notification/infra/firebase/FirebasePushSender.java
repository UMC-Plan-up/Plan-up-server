package com.planup.planup.domain.notification.infra.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.planup.planup.domain.notification.entity.device.PushSender;
import org.springframework.stereotype.Component;
import com.google.firebase.messaging.Notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Component
public class FirebasePushSender implements PushSender{

    @Override
    public String sendToToken(String token, String title, String body, Map<String, String> data) throws FirebaseMessagingException {
        var notification = Notification.builder().setTitle(title).setBody(body).build();
        var msg = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .putAllData(data == null ? Map.of() : data)
                .build();
        return FirebaseMessaging.getInstance().send(msg);
    }

    @Override
    public String sendDataOnly(String token, Map<String, String> data) throws FirebaseMessagingException {
        var msg = Message.builder().setToken(token).putAllData(data).build();
        return FirebaseMessaging.getInstance().send(msg);
    }

    @Override
    public String sendToTopic(String topic, String title, String body) throws FirebaseMessagingException {
        var msg = Message.builder()
                .setTopic(topic)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .build();
        return FirebaseMessaging.getInstance().send(msg);
    }

    @Override
    public MulticastResult sendMulticast(Collection<String> tokens, String title, String body) throws FirebaseMessagingException {
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
        return new MulticastResult(res.getSuccessCount(), res.getFailureCount(), failures);
    }
}

