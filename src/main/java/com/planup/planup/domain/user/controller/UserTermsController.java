package com.planup.planup.domain.user.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.notification.service.NotificationPreferenceService;
import com.planup.planup.domain.user.dto.AuthResponseDTO;

import com.planup.planup.domain.user.dto.NotificationPreferenceRequest;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.command.UserTermsService;
import com.planup.planup.validation.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/terms")
@RequiredArgsConstructor
public class UserTermsController {

    private final UserTermsService userTermsService;
    private final NotificationPreferenceService notificationPreferenceService;

    @PatchMapping("/preferences")
    public ApiResponse<Void> updateNotificationPreference(
            @CurrentUser User user,
            @RequestBody NotificationPreferenceRequest request
    ) {
        notificationPreferenceService.updatePreference(user, request.termsId(), request.enabled());
        return ApiResponse.onSuccess(null);
    }
}
