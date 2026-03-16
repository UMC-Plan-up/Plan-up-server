package com.planup.planup.domain.notification.service.notification;

import com.planup.planup.domain.notification.entity.notification.Notification;
import com.planup.planup.domain.notification.entity.notification.TargetType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class NotificationUrlProvider {

    public static String generate(Notification notification) {

        TargetType targetType = notification.getTargetType();
        Long targetId = notification.getTargetId();

        return switch (targetType) {
            case GOAL -> "/goals/" + targetId;
            case USER -> "/users/" + targetId;
            case REPORT -> "/reports/" + targetId;
            case CHALLENGE -> "/goals/" + targetId;
        };

    }
}
