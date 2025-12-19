package com.planup.planup.domain.notification.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.notification.dto.DeviceTokenRequestDTO;
import com.planup.planup.domain.notification.service.deviceTokenService.DeviceTokenService;
import com.planup.planup.validation.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
public class DeviceController {

    private final DeviceTokenService deviceTokenService;

    @Operation(
            summary = "디바이스 토큰 등록 또는 갱신",
            description = """
        사용자 디바이스의 FCM 토큰을 서버에 등록합니다.
        
        - 이미 존재하는 토큰인 경우: 사용자 재연결 및 활성화
        - 신규 토큰인 경우: 새로 저장
        
        로그인 직후 또는 앱 실행 시 호출됩니다.
        """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 등록/갱신 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping("post")
    public ApiResponse<Boolean> postDeviceTokenFromUser(@RequestBody DeviceTokenRequestDTO dto, @CurrentUser Long userId) {
        deviceTokenService.upsert(userId, dto.token(), dto.plateFrom(), dto.appVersion(), dto.local());
        return ApiResponse.onSuccess(true);
    }

    @Operation(
            summary = "디바이스 토큰 비활성화",
            description = """
        특정 디바이스 토큰을 비활성화합니다.
        
        - 로그아웃 시
        - 토큰 만료 또는 invalid 처리 시 사용됩니다.
        
        데이터는 삭제되지 않고 active=false 처리됩니다.
        """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 비활성화 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "토큰을 찾을 수 없음")
    })
    @PatchMapping("/deactivate/token/{token}")
    public ApiResponse<Boolean> deactivateByToken(@PathVariable String token) {
        deviceTokenService.deactivateByToken(token);
        return ApiResponse.onSuccess(true);
    }

    @Operation(
            summary = "사용자 디바이스 토큰 전체 비활성화",
            description = """
        현재 로그인한 사용자의 모든 디바이스 토큰을 비활성화합니다.
        
        - 전체 로그아웃
        - 보안 이슈 발생 시 사용
        """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "모든 토큰 비활성화 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse    (responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PatchMapping("/deactivate/userId")
    public ApiResponse<Boolean> deactivateByUserId(@CurrentUser Long userId) {
        deviceTokenService.deactivateAllByUser(userId);
        return ApiResponse.onSuccess(true);
    }
}
