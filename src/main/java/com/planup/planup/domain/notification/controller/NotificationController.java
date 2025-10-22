package com.planup.planup.domain.notification.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.notification.dto.NotificationResponseDTO;
import com.planup.planup.domain.notification.entity.NotificationType;
import com.planup.planup.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PatchMapping("/{notificationId}")
    public ApiResponse<Void> patchNotificationRead(Long userId, Long notificationId) {
        notificationService.markAsRead(notificationId, userId);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/unread/{receiverId}")
    public ApiResponse<List<NotificationResponseDTO.NotificationDTO>> getUnreadNotifications(
            @PathVariable Long receiverId) {

        List<NotificationResponseDTO.NotificationDTO> unreadNotifications =
                notificationService.getUnreadNotifications(receiverId);

        return ApiResponse.onSuccess(unreadNotifications);
    }

    @GetMapping("/unread/{receiverId}/{type}")
    public ApiResponse<List<NotificationResponseDTO.NotificationDTO>> getUnreadNotificationsWithType(
            @PathVariable Long receiverId,
            @PathVariable String type) {

        List<NotificationResponseDTO.NotificationDTO> unreadNotifications =
                notificationService.getUnreadNotificationsWithType(receiverId, type);

        return ApiResponse.onSuccess(unreadNotifications);
    }

    @GetMapping("/{userId}")
    public ApiResponse<List<NotificationResponseDTO.NotificationDTO>> getNotificationByUserId(@PathVariable Long userId) {
        List<NotificationResponseDTO.NotificationDTO> notificationDTOS = notificationService.getAllNotifications(userId);
        return ApiResponse.onSuccess(notificationDTOS);
    }
}
