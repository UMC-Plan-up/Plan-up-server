package com.planup.planup.domain.notification.service;

import com.planup.planup.domain.notification.entity.Notification;
import com.planup.planup.domain.notification.entity.TargetType;
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
            case CHALLENGE -> "/challenges/" + targetId;
        };

    }
}
