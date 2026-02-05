package com.planup.planup.domain.notification.message.listener;

import com.planup.planup.domain.notification.entity.device.DeviceToken;
import com.planup.planup.domain.notification.repository.DeviceTokenRepository;
import com.planup.planup.domain.notification.entity.device.NotificationCreatedEvent;
import com.planup.planup.domain.notification.entity.device.PushSender;
import com.planup.planup.domain.notification.message.MessageContext;
import com.planup.planup.domain.notification.message.NotificationMessageProvider;
import com.planup.planup.domain.notification.repository.NotificationRepository;
import com.planup.planup.domain.notification.service.NotificationServiceRead;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationPushListener {
    private final PushSender pushSender;
    private final DeviceTokenRepository deviceTokenRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onCreated(NotificationCreatedEvent event) {
        List<String> tokens = deviceTokenRepository.findActiveByUserId(event.receiverId()).stream().map(DeviceToken::getToken).toList();

        if (tokens.isEmpty()) return;

//        switch (event.notificationType().getGroup()) {
//            case GOAL -> "";
//            case CHALLENGE -> "";
//            case FEEDBACK -> "";
//            case ETC -> "";
//        }

        String generatedMessage = NotificationMessageProvider.generate(new MessageContext(event.notificationType(), event.senderName(), event.receiverName(), event.targetId(), event.updatedPartsStr(), null));

        PushSender.MulticastResult multicastResult = pushSender.sendMulticast(tokens, generatedMessage, "클릭해 확인해 보세요!");
    }
}
