package com.planup.planup.domain.user.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.user.dto.*;
import com.planup.planup.domain.user.service.command.UserAuthCommandService;
import com.planup.planup.domain.user.service.query.UserQueryService;
import com.planup.planup.validation.annotation.CurrentUser;
import com.planup.planup.validation.jwt.dto.TokenRefreshRequestDTO;
import com.planup.planup.validation.jwt.dto.TokenRefreshResponseDTO;
import com.planup.planup.validation.jwt.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.planup.planup.domain.user.dto.OAuthRequestDTO;
import com.planup.planup.domain.user.dto.OAuthResponseDTO;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/users")
public class UserAuthController implements UserAuthControllerDocs {

    private final UserAuthCommandService userAuthCommandService;
    private final TokenService tokenService;
    private final UserQueryService userQueryService;

    // HTML 응답을 생성하는 공통 메서드
    private ResponseEntity<String> createHtmlResponse(String html) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
                .body(html);
    }

    // ======================== 기본 인증 (가입/로그인/토큰) ========================

    @Override
    @PostMapping("/signup")
    public ApiResponse<UserResponseDTO.AuthResponseDTO> signup(@Valid @RequestBody UserRequestDTO.Signup request) {
        UserResponseDTO.AuthResponseDTO result = userAuthCommandService.signup(request);
        return ApiResponse.onSuccess(result);
    }

    @Override
    @PostMapping("/login")
    public ApiResponse<UserResponseDTO.AuthResponseDTO> login(@Valid @RequestBody UserRequestDTO.Login request) {
        UserResponseDTO.AuthResponseDTO result = userAuthCommandService.login(request);
        return ApiResponse.onSuccess(result);
    }

    @Override
    @PostMapping("/logout")
    public ApiResponse<String> logout(@CurrentUser Long userId, HttpServletRequest httpRequest) {
        userAuthCommandService.logout(userId, httpRequest);
        return ApiResponse.onSuccess("로그아웃되었습니다");
    }

    @Override
    @PostMapping("/refresh")
    public ApiResponse<TokenRefreshResponseDTO> refreshToken(@Valid @RequestBody TokenRefreshRequestDTO request) {
        TokenRefreshResponseDTO response = tokenService.refreshAccessToken(
                request.getRefreshToken()
        );
        return ApiResponse.onSuccess(response);
    }

    @Override
    @GetMapping("/validate")
    public ApiResponse<String> validateToken(@CurrentUser Long userId) {
        return ApiResponse.onSuccess("토큰이 유효합니다");
    }

    // ======================== 계정 검증 및 복구 (이메일/비밀번호) ========================

    @Override
    @GetMapping("/email/check-duplicate")
    public ApiResponse<AuthResponseDTO.EmailDuplicate> checkEmailDuplicate(@RequestParam String email) {
        AuthResponseDTO.EmailDuplicate response = userQueryService.checkEmailDuplicate(email);
        return ApiResponse.onSuccess(response);
    }

    @Override
    @PostMapping("/password/change")
    public ApiResponse<Boolean> changePasswordWithToken(@RequestBody UserRequestDTO.PasswordChangeWithToken request, @CurrentUser Long userId) {
        userAuthCommandService.changePassword(userId, request.getNewPassword());
        return ApiResponse.onSuccess(true);
    }

    @Override
    @PostMapping("/email/send")
    public ApiResponse<AuthResponseDTO.EmailSend> sendEmailVerification(@RequestBody @Valid AuthRequestDTO.EmailVerification request) {
        AuthResponseDTO.EmailSend response = userAuthCommandService.sendEmailVerification(request.getEmail());
        return ApiResponse.onSuccess(response);
    }

    @Override
    @GetMapping("/email/verification-status")
    public ApiResponse<AuthResponseDTO.EmailVerificationStatus> getEmailVerificationStatus(@RequestParam("token") String token) {
        AuthResponseDTO.EmailVerificationStatus response = userQueryService.getEmailVerificationStatus(token);
        return ApiResponse.onSuccess(response);
    }

    @Override
    @GetMapping("/email/verify-link")
    public ResponseEntity<String> handleEmailLink(@RequestParam String token) {
        String html = userAuthCommandService.handleEmailVerificationLink(token);
        return createHtmlResponse(html);
    }

    @Override
    @PostMapping("/password/change-email/send")
    public ApiResponse<AuthResponseDTO.EmailSend> sendPasswordChangeEmail(
            @RequestBody @Valid UserRequestDTO.PasswordChangeEmail request) {
        AuthResponseDTO.EmailSend response = userAuthCommandService.sendPasswordChangeEmail(
                request.getEmail(),
                request.getIsLoggedIn() // 로그인 상태 추가
        );
        return ApiResponse.onSuccess(response);
    }

    @Override
    @GetMapping("/password/change-link")
    public ResponseEntity<String> handlePasswordChangeLink(@RequestParam String token) {
        String html = userAuthCommandService.handlePasswordChangeLink(token);
        return createHtmlResponse(html);
    }

    // ======================== 소셜 인증 및 연동 ========================

    @Override
    @PostMapping("/auth/kakao")
    public ApiResponse<UserResponseDTO.AuthResponseDTO> kakaoAuth(@Valid @RequestBody OAuthRequestDTO.KakaoAuth request) {
        UserResponseDTO.AuthResponseDTO result = userAuthCommandService.kakaoAuth(request);
        return ApiResponse.onSuccess(result);
    }

    @Override
    @PostMapping("/auth/kakao/complete")
    public ApiResponse<UserResponseDTO.AuthResponseDTO> kakaoSignupComplete(@Valid @RequestBody OAuthRequestDTO.KaKaoSignup request) {
        UserResponseDTO.AuthResponseDTO result = userAuthCommandService.kakaoSignupComplete(request);
        return ApiResponse.onSuccess(result);
    }

    @Override
    @GetMapping("/auth/kakao/linked")
    public ApiResponse<OAuthResponseDTO.KakaoLinkStatus> getKakaoLinkStatus(@CurrentUser Long userId) {
        OAuthResponseDTO.KakaoLinkStatus result = userQueryService.getKakaoLinkStatus(userId);
        return ApiResponse.onSuccess(result);
    }

    @Override
    @PostMapping("/auth/kakao/link")
    public ApiResponse<OAuthResponseDTO.KaKaoLink> linkKakaoAccount(@CurrentUser Long userId, @Valid @RequestBody OAuthRequestDTO.KaKaoLink request) {
        OAuthResponseDTO.KaKaoLink result = userAuthCommandService.linkKakaoAccount(userId, request);
        return ApiResponse.onSuccess(result);
    }

    // ======================== 초대 코드 처리 ========================

    @Override
    @GetMapping("/me/invite-code")
    public ApiResponse<AuthResponseDTO.InviteCode> getMyInviteCode(
            @CurrentUser Long userId) {
        AuthResponseDTO.InviteCode response = userQueryService.getMyInviteCode(userId);
        return ApiResponse.onSuccess(response);
    }

    @Override
    @PostMapping("/invite-code/process")
    public ApiResponse<AuthResponseDTO.InviteCodeProcess> processInviteCode(
            @Valid @RequestBody AuthRequestDTO.InviteCode request,
            @CurrentUser Long userId) {
        AuthResponseDTO.InviteCodeProcess response = userAuthCommandService.processInviteCode(request.getInviteCode(), userId);
        return ApiResponse.onSuccess(response);
    }

    @Override
    @PostMapping("/invite-code/validate")
    public ApiResponse<AuthResponseDTO.ValidateInviteCode> validateInviteCode(
            @Valid @RequestBody AuthRequestDTO.InviteCode request) {
        AuthResponseDTO.ValidateInviteCode response = userQueryService.validateInviteCode(request.getInviteCode());
        return ApiResponse.onSuccess(response);
    }

    // ======================== 회원 탈퇴 ========================

    @Override
    @PostMapping("/withdraw")
    public ApiResponse<UserResponseDTO.Withdrawal> withdrawUser(
            @Valid @RequestBody UserRequestDTO.Withdrawal request,
            @CurrentUser Long userId) {
        UserResponseDTO.Withdrawal response = userAuthCommandService.withdrawUser(userId, request);
        return ApiResponse.onSuccess(response);
    }
}
