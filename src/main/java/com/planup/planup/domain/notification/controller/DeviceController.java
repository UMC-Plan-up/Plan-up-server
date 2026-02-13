package com.planup.planup.domain.notification.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.notification.controller.docs.DeviceControllerDocs;
import com.planup.planup.domain.notification.dto.DeviceTokenRequestDTO;
import com.planup.planup.domain.notification.service.deviceTokenService.DeviceTokenService;
import com.planup.planup.validation.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/deviceToken")
@Tag(
        name = "Device Token",
        description = "푸시 알림을 위한 디바이스 토큰 등록/관리 API"
)
public class DeviceController implements DeviceControllerDocs {

    private final DeviceTokenService deviceTokenService;
    @Override
    @PostMapping("post")
    public ApiResponse<Boolean> postDeviceTokenFromUser(@RequestBody DeviceTokenRequestDTO dto, @CurrentUser Long userId) {
        deviceTokenService.upsert(userId, dto.token(), dto.platform(), dto.appVersion(), dto.local());
        return ApiResponse.onSuccess(true);
    }

    @Override
    @PatchMapping("/deactivate/token/{token}")
    public ApiResponse<Boolean> deactivateByToken(@PathVariable String token) {
        deviceTokenService.deactivateByToken(token);
        return ApiResponse.onSuccess(true);
    }

    @Override
    @PatchMapping("/deactivate/userId")
    public ApiResponse<Boolean> deactivateByUserId(@CurrentUser Long userId) {
        deviceTokenService.deactivateAllByUser(userId);
        return ApiResponse.onSuccess(true);
    }
}
