package com.planup.planup.domain.user.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.user.dto.*;
import com.planup.planup.domain.user.service.command.UserAuthCommandService;
import com.planup.planup.domain.user.service.command.UserProfileCommandService;
import com.planup.planup.domain.user.service.query.UserQueryService;
import com.planup.planup.validation.annotation.CurrentUser;
import com.planup.planup.validation.jwt.dto.TokenRefreshRequestDTO;
import com.planup.planup.validation.jwt.dto.TokenRefreshResponseDTO;
import com.planup.planup.validation.jwt.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
public class UserAuthController {

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

    @Operation(summary = "회원가입", description = "이메일/비밀번호로 새 계정을 생성합니다")
    @PostMapping("/signup")
    public ApiResponse<UserResponseDTO.Signup> signup(@Valid @RequestBody UserRequestDTO.Signup request) {
        UserResponseDTO.Signup result = userAuthCommandService.signup(request);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인하여 JWT 토큰을 발급받습니다")
    @PostMapping("/login")
    public ApiResponse<UserResponseDTO.Login> login(@Valid @RequestBody UserRequestDTO.Login request) {
        UserResponseDTO.Login result = userAuthCommandService.login(request);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "로그아웃", description = "현재 사용자를 로그아웃합니다")
    @PostMapping("/logout")
    public ApiResponse<String> logout(@Parameter(hidden = true) @CurrentUser Long userId, HttpServletRequest httpRequest) {
        userAuthCommandService.logout(userId, httpRequest);
        return ApiResponse.onSuccess("로그아웃되었습니다");
    }

    @Operation(summary = "토큰 갱신", description = "리프레쉬 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다")
    @PostMapping("/refresh")
    public ApiResponse<TokenRefreshResponseDTO> refreshToken(@Valid @RequestBody TokenRefreshRequestDTO request) {
        TokenRefreshResponseDTO response = tokenService.refreshAccessToken(
                request.getRefreshToken()
        );
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "토큰 유효성 확인", description = "현재 액세스 토큰의 유효성을 확인합니다")
    @GetMapping("/validate")
    public ApiResponse<String> validateToken(@Parameter(hidden = true) @CurrentUser Long userId) {
        return ApiResponse.onSuccess("토큰이 유효합니다");
    }

    // ======================== 계정 검증 및 복구 (이메일/비밀번호) ========================

    @Operation(summary = "이메일 중복 확인", description = "이메일이 이미 사용 중인지 확인합니다")
    @GetMapping("/email/check-duplicate")
    public ApiResponse<AuthResponseDTO.EmailDuplicate> checkEmailDuplicate(@RequestParam String email) {
        AuthResponseDTO.EmailDuplicate response = userQueryService.checkEmailDuplicate(email);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "비밀번호 변경", description = "이메일 인증 토큰으로 비밀번호를 변경한다.")
    @PostMapping("/password/change")
    public ApiResponse<Boolean> changePasswordWithToken(@RequestBody UserRequestDTO.PasswordChangeWithToken request) {
        userAuthCommandService.changePasswordWithToken(request.getToken(), request.getNewPassword());
        return ApiResponse.onSuccess(true);
    }

    @Operation(summary = "이메일 인증 발송", description = "이메일 중복 확인 후 인증메일을 발송하고 토큰을 반환합니다")
    @PostMapping("/email/send")
    public ApiResponse<AuthResponseDTO.EmailSend> sendEmailVerification(@RequestBody @Valid AuthRequestDTO.EmailVerification request) {
        AuthResponseDTO.EmailSend response = userAuthCommandService.sendEmailVerification(request.getEmail());
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "이메일 인증 재발송")
    @PostMapping("/email/resend")
    public ApiResponse<AuthResponseDTO.EmailSend> resendVerificationEmail(@RequestBody @Valid AuthRequestDTO.EmailVerification request) {
        AuthResponseDTO.EmailSend response = userAuthCommandService.resendEmailVerification(request.getEmail());
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "이메일 인증 여부 확인", description = "토큰으로 이메일을 확인하고 인증 상태를 반환합니다")
    @GetMapping("/email/verification-status")
    public ApiResponse<AuthResponseDTO.EmailVerificationStatus> getEmailVerificationStatus(@RequestParam("token") String token) {
        AuthResponseDTO.EmailVerificationStatus response = userQueryService.getEmailVerificationStatus(token);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "이메일 링크 클릭 처리", description = "이메일 링크 클릭 시 인증 처리 후 웹페이지 표시")
    @GetMapping("/email/verify-link")
    public ResponseEntity<String> handleEmailLink(@RequestParam String token) {
        String html = userAuthCommandService.handleEmailVerificationLink(token);
        return createHtmlResponse(html);
    }

    @Operation(summary = "비밀번호 변경 확인 이메일 발송", description = "비밀번호 변경을 위한 확인 메일을 발송하고 토큰을 반환합니다")
    @PostMapping("/password/change-email/send")
    public ApiResponse<AuthResponseDTO.EmailSend> sendPasswordChangeEmail(
            @RequestBody @Valid UserRequestDTO.PasswordChangeEmail request) {
        AuthResponseDTO.EmailSend response = userAuthCommandService.sendPasswordChangeEmail(
                request.getEmail(),
                request.getIsLoggedIn() // 로그인 상태 추가
        );
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "비밀번호 변경 확인 이메일 재발송", description = "비밀번호 변경을 위한 확인 메일을 재발송합니다")
    @PostMapping("/password/change-email/resend")
    public ApiResponse<AuthResponseDTO.EmailSend> resendPasswordChangeEmail(
            @RequestBody @Valid UserRequestDTO.PasswordChangeEmail request) {
        AuthResponseDTO.EmailSend response = userAuthCommandService.resendPasswordChangeEmail(
                request.getEmail(),
                request.getIsLoggedIn()  // 로그인 상태 포함
        );
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "비밀번호 변경 요청 이메일 링크 클릭 처리", description = "비밀번호 변경 링크 클릭 시 확인 처리 후 웹페이지 표시")
    @GetMapping("/password/change-link")
    public ResponseEntity<String> handlePasswordChangeLink(@RequestParam String token) {
        String html = userAuthCommandService.handlePasswordChangeLink(token);
        return createHtmlResponse(html);
    }

    // ======================== 소셜 인증 및 연동 ========================

    @Operation(summary = "카카오 소셜 인증",
            description = "카카오 인가코드로 로그인/회원가입 여부를 판단합니다")
    @PostMapping("/auth/kakao")
    public ApiResponse<OAuthResponseDTO.KakaoAuth> kakaoAuth(@Valid @RequestBody OAuthRequestDTO.KakaoAuth request) {
        OAuthResponseDTO.KakaoAuth result = userAuthCommandService.kakaoAuth(request);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "카카오 회원가입 완료",
            description = "카카오 온보딩 완료 후 모든 정보를 받아서 회원가입을 완료합니다")
    @PostMapping("/auth/kakao/complete")
    public ApiResponse<UserResponseDTO.Signup> kakaoSignupComplete(@Valid @RequestBody OAuthRequestDTO.KaKaoSignup request) {
        UserResponseDTO.Signup result = userAuthCommandService.kakaoSignupComplete(request);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "이메일 인증 대안 - 카카오 로그인",
            description = "이메일 인증 실패 시 카카오 소셜 로그인으로 전환합니다")
    @PostMapping("/auth/email/alternative")
    public ApiResponse<OAuthResponseDTO.KakaoAuth> emailAuthAlternative(@Valid @RequestBody OAuthRequestDTO.KakaoAuth request) {
        OAuthResponseDTO.KakaoAuth result = userAuthCommandService.kakaoAuth(request);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "카카오 계정 연동 여부 조회",
            description = "현재 로그인한 사용자의 카카오 계정 연동 여부를 확인합니다.")
    @GetMapping("/auth/kakao/linked")
    public ApiResponse<OAuthResponseDTO.KakaoLinkStatus> getKakaoLinkStatus(@CurrentUser Long userId) {
        OAuthResponseDTO.KakaoLinkStatus result = userQueryService.getKakaoLinkStatus(userId);
        return ApiResponse.onSuccess(result);
    }

    // ======================== 초대 코드 처리 ========================

    @Operation(summary = "내 초대코드 조회", description = "내 초대코드를 조회하거나 새로 생성합니다")
    @GetMapping("/me/invite-code")
    public ApiResponse<AuthResponseDTO.InviteCode> getMyInviteCode(
            @Parameter(hidden = true) @CurrentUser Long userId) {
        AuthResponseDTO.InviteCode response = userQueryService.getMyInviteCode(userId);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "초대코드 처리", description = "초대코드를 검증하고 친구 관계를 생성합니다")
    @PostMapping("/invite-code/process")
    public ApiResponse<AuthResponseDTO.InviteCodeProcess> processInviteCode(
            @Valid @RequestBody AuthRequestDTO.InviteCode request,
            @Parameter(hidden = true) @CurrentUser Long userId) {
        AuthResponseDTO.InviteCodeProcess response = userAuthCommandService.processInviteCode(request.getInviteCode(), userId);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "초대코드 실시간 검증", description = "입력된 초대코드가 유효한지 실시간으로 검증합니다")
    @PostMapping("/invite-code/validate")
    public ApiResponse<AuthResponseDTO.ValidateInviteCode> validateInviteCode(
            @Valid @RequestBody AuthRequestDTO.InviteCode request) {
        AuthResponseDTO.ValidateInviteCode response = userQueryService.validateInviteCode(request.getInviteCode());
        return ApiResponse.onSuccess(response);
    }

    // ======================== 회원 탈퇴 ========================

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 처리하고 탈퇴 이유를 저장합니다")
    @PostMapping("/withdraw")
    public ApiResponse<UserResponseDTO.Withdrawal> withdrawUser(
            @Valid @RequestBody UserRequestDTO.Withdrawal request,
            @Parameter(hidden = true) @CurrentUser Long userId) {
        UserResponseDTO.Withdrawal response = userAuthCommandService.withdrawUser(userId, request);
        return ApiResponse.onSuccess(response);
    }
}