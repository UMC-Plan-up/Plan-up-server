package com.planup.planup.domain.notification.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.notification.dto.NotificationResponseDTO;
import com.planup.planup.domain.notification.service.NotificationServiceRead;
import com.planup.planup.domain.notification.service.NotificationServiceWrite;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationServiceRead notificationServiceRead;
    private final NotificationServiceWrite notificationServiceWrite;

    @PatchMapping("/{notificationId}")
    public ApiResponse<Void> patchNotificationRead(Long userId, Long notificationId) {
        notificationServiceWrite.markAsRead(notificationId, userId);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/unread/{receiverId}")
    public ApiResponse<List<NotificationResponseDTO.NotificationDTO>> getUnreadNotifications(
            @PathVariable Long receiverId) {

        List<NotificationResponseDTO.NotificationDTO> unreadNotifications =
                notificationServiceRead.getUnreadNotifications(receiverId);

        return ApiResponse.onSuccess(unreadNotifications);
    }

    @GetMapping("/unread/{receiverId}/{type}")
    public ApiResponse<List<NotificationResponseDTO.NotificationDTO>> getUnreadNotificationsWithType(
            @PathVariable Long receiverId,
            @PathVariable String type) {

        List<NotificationResponseDTO.NotificationDTO> unreadNotifications =
                notificationServiceRead.getUnreadNotificationsWithType(receiverId, type);

        return ApiResponse.onSuccess(unreadNotifications);
    }

    @GetMapping("/{userId}")
    public ApiResponse<List<NotificationResponseDTO.NotificationDTO>> getNotificationByUserId(@PathVariable Long userId) {
        List<NotificationResponseDTO.NotificationDTO> notificationDTOS = notificationServiceRead.getAllNotifications(userId);
        return ApiResponse.onSuccess(notificationDTOS);
    }
}
