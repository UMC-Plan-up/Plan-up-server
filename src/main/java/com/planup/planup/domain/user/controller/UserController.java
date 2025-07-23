package com.planup.planup.domain.user.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "nickname 변경 요청", description = "닉네임을 변경하기 위해 기존 닉네임 호출")
    @GetMapping("/mypage/profile/nickname")
    public ApiResponse<String> updateNicknameReq(Long userId) {
        String nickname = userService.getNickname(userId);
        return ApiResponse.onSuccess(nickname);
    }

    @Operation(summary = "새로운 nickname으로 수정", description = "입력된 닉네임을 중복 닉네임이 있는지 학인하고 수정")
    @PostMapping("/mypage/profile/nickname")
    public ApiResponse<String> updateNickname(Long userId, @RequestBody String nickname) {
        String newNickname = userService.updateNickname(userId, nickname);
        return ApiResponse.onSuccess(newNickname);
    }

    @Operation(summary = "해택 및 마케팅 동의 변경", description = "해택 동의가 되어있다면 비확성화, 동의가 안되어있으면 동의로 변경")
    @PatchMapping("/mypage/notification/agree")
    public ApiResponse<Boolean> updateNotificationAgree(Long userId) {
        userService.updateNotificationAgree(userId);
        return ApiResponse.onSuccess(true);
    }

    @Operation(summary = "비밀변호 변경 위해 기존 비밀번호 입력", description = "입력된 기존 비밀번호를 확인하고 true/false 반환")
    @PostMapping("/mypage/profile/password")
    public ApiResponse<Boolean> checkPassword(Long userId, String password) {
        boolean result = userService.checkPassword(userId, password);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "비밀번호 변경", description = "사용자로부터 새로운 비밀번호를 입력받고 변경한다.")
    @PostMapping("/mypage/profile/password/update")
    public ApiResponse<Boolean> updatePassword(Long userId, String password) {
        userService.updatePassword(userId, password);
        return ApiResponse.onSuccess(true);
    }
}