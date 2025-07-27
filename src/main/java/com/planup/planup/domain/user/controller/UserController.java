package com.planup.planup.domain.user.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.user.dto.UserInfoResponseDTO;
import com.planup.planup.domain.user.dto.LoginRequestDTO;
import com.planup.planup.domain.user.dto.LoginResponseDTO;
import com.planup.planup.domain.user.dto.SignupRequestDTO;
import com.planup.planup.domain.user.dto.SignupResponseDTO;
import com.planup.planup.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.planup.planup.domain.user.dto.KakaoAccountResponseDTO;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RestController;
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

    @Operation(summary = "프로필 사진 변경", description = "마이페이지에서 프로필 사진 변경")
    @PostMapping("/mypage/profile/image")
    public ApiResponse<String> updateProfileImage(
            @RequestParam Long userId,
            @RequestPart MultipartFile imageFile) {
        String imageUrl = userService.updateProfileImage(userId, imageFile);
        return ApiResponse.onSuccess(imageUrl);
    }

    @Operation(summary = "유저 정보 조회", description = "유저의 상세 정보 조회")
    @GetMapping("/users/info")
    public ApiResponse<UserInfoResponseDTO> getUserInfo(@RequestParam Long userId) {
        UserInfoResponseDTO userInfo = userService.getUserInfo(userId);
        return ApiResponse.onSuccess(userInfo);
    }
    @Operation(summary = "이메일 변경", description = "유저 이메일 변경")
    @PostMapping("/mypage/profile/email")
    public ApiResponse<String> updateEmail(
            @RequestParam Long userId,
            @RequestParam String newEmail) {
        String email = userService.updateEmail(userId, newEmail);
        return ApiResponse.onSuccess(email);
    }

    @Operation(summary = "회원가입", description = "이메일/비밀번호로 새 계정을 생성합니다")
    @PostMapping("/users/signup")
    public ApiResponse<SignupResponseDTO> signup(@Valid @RequestBody SignupRequestDTO request) {
        SignupResponseDTO result = userService.signup(request);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인하여 JWT 토큰을 발급받습니다")
    @PostMapping("/users/login")
    public ApiResponse<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO result = userService.login(request);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "로그아웃", description = "현재 세션을 종료합니다")
    @PostMapping("/users/logout")
    public ApiResponse<String> logout() {
        // 간단한 로그아웃 구현 (JWT는 클라이언트에서 삭제)
        return ApiResponse.onSuccess("로그아웃이 완료되었습니다");
    }

    @Operation(summary = "카카오톡 계정 연동 상태 확인", description = "사용자의 카카오톡 계정 연동 여부와 연동된 이메일을 확인합니다")
    @GetMapping("/mypage/kakao-account")
    public ApiResponse<KakaoAccountResponseDTO> getKakaoAccountStatus(@RequestParam Long userId) {
        KakaoAccountResponseDTO kakaoAccountStatus = userService.getKakaoAccountStatus(userId);
        return ApiResponse.onSuccess(kakaoAccountStatus);
    }
}