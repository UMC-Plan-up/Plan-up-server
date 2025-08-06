package com.planup.planup.domain.user.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.domain.user.dto.*;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.EmailService;
import com.planup.planup.domain.user.service.UserService;
import com.planup.planup.validation.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.net.URI;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Operation(summary = "nickname 변경 요청", description = "닉네임을 변경하기 위해 기존 닉네임 호출")
    @GetMapping("/mypage/profile/nickname")
    public ApiResponse<String> updateNicknameReq(Long userId) {
        String nickname = userService.getNickname(userId);
        return ApiResponse.onSuccess(nickname);
    }

    @Operation(summary = "새로운 nickname으로 수정", description = "입력된 닉네임을 중복 닉네임이 있는지 확인하고 수정")
    @PostMapping("/mypage/profile/nickname")
    public ApiResponse<String> updateNickname(Long userId, @RequestBody String nickname) {
        String newNickname = userService.updateNickname(userId, nickname);
        return ApiResponse.onSuccess(newNickname);
    }

    @Operation(summary = "혜택 및 마케팅 동의 변경", description = "혜택 동의가 되어있다면 비활성화, 동의가 안되어있으면 동의로 변경")
    @PatchMapping("/mypage/notification/agree")
    public ApiResponse<Boolean> updateNotificationAgree(Long userId) {
        userService.updateNotificationAgree(userId);
        return ApiResponse.onSuccess(true);
    }

    @Operation(summary = "비밀번호 변경 위해 기존 비밀번호 입력", description = "입력된 기존 비밀번호를 확인하고 true/false 반환")
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

    @Operation(summary = "프로필 사진 업로드 및 변경", description = "회원가입 시 프로필 사진을 업로드하거나 변경합니다.")
    @PostMapping(value = "/profile/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImageUploadResponseDTO> uploadProfileImage(
            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestPart("file") MultipartFile file,
            @Parameter(hidden = true) @CurrentUser User currentUser) {

        ImageUploadResponseDTO response = userService.uploadProfileImage(file, currentUser);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "내 초대코드 조회", description = "내 초대코드를 조회하거나 새로 생성합니다")
    @GetMapping("/users/me/invite-code")
    public ApiResponse<InviteCodeResponseDTO> getMyInviteCode(
            @Parameter(hidden = true) @CurrentUser User currentUser) {
        InviteCodeResponseDTO response = userService.getMyInviteCode(currentUser.getId());
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "초대코드 실시간 검증", description = "입력된 초대코드가 유효한지 실시간으로 검증합니다")
    @PostMapping("/users/invite-code/validate")
    public ApiResponse<ValidateInviteCodeResponseDTO> validateInviteCode(
            @Valid @RequestBody ValidateInviteCodeRequestDTO request,
            @Parameter(hidden = true) @CurrentUser User currentUser) {
        ValidateInviteCodeResponseDTO response = userService.validateInviteCode(request.getInviteCode(), currentUser.getId());
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "이메일 인증 완료")
    @GetMapping("/users/email/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        try {
            String email = emailService.verifyToken(token);
            userService.markEmailAsVerified(email);

            // 프로필 설정 페이지로 리다이렉트
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontendUrl + "/profile/setup?email=" + email))
                    .build();

        } catch (Exception e) {
            // 인증 실패 페이지로 리다이렉트
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontendUrl + "/auth/verification-failed"))
                    .build();
        }
    }

    @Operation(summary = "이메일 인증 재발송")
    @PostMapping("/users/email/resend")
    public ApiResponse<String> resendVerificationEmail (@RequestParam String email) {
        emailService.resendVerificationLink(email);
        return ApiResponse.onSuccess("인증 이메일이 재발송되었습니다.");
    }
}