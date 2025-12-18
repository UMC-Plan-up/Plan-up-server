package com.planup.planup.domain.notification.controller;

import com.google.protobuf.Api;
import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.notification.dto.DeviceTokenRequestDTO;
import com.planup.planup.domain.notification.service.deviceTokenService.DeviceTokenService;
import com.planup.planup.validation.annotation.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/deviceToken")
public class DeviceController {

    private final DeviceTokenService deviceTokenService;

    @PostMapping("post")
    public ApiResponse<Boolean> postDeviceTokenFromUser(@RequestBody DeviceTokenRequestDTO dto, @CurrentUser Long userId) {
        deviceTokenService.upsert(userId, dto.token(), dto.plateFrom(), dto.appVersion(), dto.local());
        return ApiResponse.onSuccess(true);
    }

    @PatchMapping("/deactivate/token/{token}")
    public ApiResponse<Boolean> deactivateByToken(@PathVariable String token) {
        deviceTokenService.deactivateByToken(token);
        return ApiResponse.onSuccess(true);
    }

    @PatchMapping("/deactivate/userId")
    public ApiResponse<Boolean> deactivateByUserId(@CurrentUser Long userId) {
        deviceTokenService.deactivateAllByUser(userId);
        return ApiResponse.onSuccess(true);
    }
}
