package com.planup.planup.domain.notification.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.notification.dto.NotificationReadRequest;
import com.planup.planup.domain.notification.dto.NotificationResponseDTO;
import com.planup.planup.domain.notification.entity.notification.TargetType;
import com.planup.planup.domain.notification.service.notification.NotificationQueryService;
import com.planup.planup.domain.notification.service.notification.NotificationCommandService;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.validation.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationQueryService notificationServiceRead;
    private final NotificationCommandService notificationServiceWrite;

    @Operation(
            summary = "단일 알림 읽음 처리",
            description = "특정 알림 1개를 읽음 상태로 변경합니다."
    )
    @PatchMapping("/{notificationId}")
    public ApiResponse<Void> patchNotificationRead(@CurrentUser Long userId, @PathVariable Long notificationId) {
        notificationServiceWrite.markAsRead(notificationId, userId);
        return ApiResponse.onSuccess(null);
    }

    @Operation(
            summary = "여러 알림 읽음 처리",
            description = "알림 ID 목록을 받아 여러 알림을 한 번에 읽음 상태로 변경합니다."
    )
    @PatchMapping("/list")
    public ApiResponse<Void> patchNotificationListRead(@CurrentUser Long userId, @RequestBody NotificationReadRequest request) {
        notificationServiceWrite.markAsRead(request, userId);
        return ApiResponse.onSuccess(null);
    }

    @Operation(
            summary = "읽지 않은 알림 조회",
            description = "현재 로그인한 사용자의 읽지 않은 알림 목록을 조회합니다."
    )
    @GetMapping("/unread")
    public ApiResponse<List<NotificationResponseDTO.NotificationDTO>> getUnreadNotifications(
            @CurrentUser Long receiverId) {

        List<NotificationResponseDTO.NotificationDTO> unreadNotifications =
                notificationServiceRead.getUnreadNotifications(receiverId);

        return ApiResponse.onSuccess(unreadNotifications);
    }

    @GetMapping("/unread/{receiverId}/{type}")
    public ApiResponse<List<NotificationResponseDTO.NotificationDTO>> getUnreadNotificationsWithType(
            @PathVariable Long receiverId,
            @PathVariable TargetType type) {

        List<NotificationResponseDTO.NotificationDTO> unreadNotifications =
                notificationServiceRead.getUnreadNotificationsWithTargetType(receiverId, type);

        return ApiResponse.onSuccess(unreadNotifications);
    }


    @Operation(
            summary = "전체 알림 조회",
            description = "현재 로그인한 사용자의 전체 알림 목록을 조회합니다."
    )
    @GetMapping()
    public ApiResponse<List<NotificationResponseDTO.NotificationDTO>> getNotificationByUserId(@CurrentUser User user) {
        List<NotificationResponseDTO.NotificationDTO> notificationDTOS = notificationServiceRead.getAllNotifications(user);
        return ApiResponse.onSuccess(notificationDTOS);
    }
}
