package com.planup.planup.domain.notification.controller.docs;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.notification.dto.DeviceTokenRequestDTO;
import com.planup.planup.validation.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(
        name = "Device Token",
        description = "푸시 알림을 위한 디바이스 토큰 등록/관리 API"
)
public interface DeviceControllerDocs {

    @Operation(
            summary = "디바이스 토큰 등록 또는 갱신",
            description = """
            사용자 디바이스의 FCM 토큰을 서버에 등록합니다.
            - 이미 존재하는 토큰: 재연결
            - 신규 토큰: 새로 저장
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ApiResponse<Boolean> postDeviceTokenFromUser(
            @RequestBody DeviceTokenRequestDTO dto,
            @CurrentUser Long userId
    );

    @Operation(summary = "디바이스 토큰 비활성화")
    ApiResponse<Boolean> deactivateByToken(@PathVariable String token);

    @Operation(summary = "사용자 전체 디바이스 토큰 비활성화")
    ApiResponse<Boolean> deactivateByUserId(@CurrentUser Long userId);
}
