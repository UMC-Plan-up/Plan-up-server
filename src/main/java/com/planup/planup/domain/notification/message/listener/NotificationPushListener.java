package com.planup.planup.domain.notification.message.listener;

import com.planup.planup.domain.notification.entity.device.DeviceToken;
import com.planup.planup.domain.notification.repository.DeviceTokenRepository;
import com.planup.planup.domain.notification.entity.device.NotificationCreatedEvent;
import com.planup.planup.domain.notification.entity.device.PushSender;
import com.planup.planup.domain.notification.message.MessageContext;
import com.planup.planup.domain.notification.message.NotificationMessageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.MultiMap;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationPushListener {
    private final PushSender pushSender;
    private final DeviceTokenRepository deviceTokenRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onCreated(NotificationCreatedEvent event) {
        log.info("[NotificationCreatedEvent] received - notificationId={}, receiverId={}, type={}",
                event.notificationId(), event.receiverId(), event.notificationType());

        try {
            List<String> tokens = deviceTokenRepository.findActiveByUserId(event.receiverId())
                    .stream()
                    .map(DeviceToken::getToken)
                    .toList();

            log.info("[NotificationCreatedEvent] tokens fetched - receiverId={}, tokenCount={}",
                    event.receiverId(), tokens.size());

            if (tokens.isEmpty()) {
                log.info("[NotificationCreatedEvent] no active tokens - receiverId={}", event.receiverId());
                return;
            }

            String generatedMessage = NotificationMessageProvider.generate(
                    new MessageContext(
                            event.notificationType(),
                            event.senderName(),
                            event.receiverName(),
                            event.targetId(),
                            event.updatedPartsStr(),
                            null
                    )
            );

            log.info("[NotificationCreatedEvent] message generated - receiverId={}, message={}",
                    event.receiverId(), generatedMessage);

            Map<String, String> data = new HashMap<>();
            data.put("notificationId", event.notificationId() != null ? String.valueOf(event.notificationId()) : "");
            data.put("receiverId", event.receiverId() != null ? String.valueOf(event.receiverId()) : "");
            data.put("group", event.group() != null ? event.group().name() : "");
            data.put("notificationType", event.notificationType() != null ? event.notificationType().name() : "");
            data.put("targetType", event.targetType() != null ? event.targetType().name() : "");
            data.put("targetId", event.targetId() != null ? String.valueOf(event.targetId()) : "");
            data.put("senderId", event.senderId() != null ? String.valueOf(event.senderId()) : "");
            data.put("senderName", event.senderName() != null ? event.senderName() : "");
            data.put("senderProfile", event.senderProfile() != null ? event.senderProfile() : "");
            data.put("receiverName", event.receiverName() != null ? event.receiverName() : "");
            data.put("updatedPartsStr", event.updatedPartsStr() != null ? event.updatedPartsStr() : "");

            log.info("[FCM] send start - receiverId={}, tokenCount={}", event.receiverId(), tokens.size());

            PushSender.MulticastResult multicastResult =
                    pushSender.sendMulticast(tokens, generatedMessage, "클릭해 확인해 보세요!", data);

            log.info("[FCM] send result - receiverId={}, result={}", event.receiverId(), multicastResult);

        } catch (Exception e) {
            log.error("[FCM] send failed - notificationId={}, receiverId={}",
                    event.notificationId(), event.receiverId(), e);
        }
    }
}
