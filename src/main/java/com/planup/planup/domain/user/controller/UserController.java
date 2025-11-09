package com.planup.planup.domain.user.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.user.dto.*;
import com.planup.planup.domain.user.service.RandomNicknameService;
import com.planup.planup.domain.user.service.UserService;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final RandomNicknameService randomNicknameService;
    private final TokenService tokenService;

    @Operation(summary = "nickname 변경 요청", description = "닉네임을 변경하기 위해 기존 닉네임 호출")
    @GetMapping("/mypage/profile/nickname")
    public ApiResponse<String> updateNicknameReq(@Parameter(hidden = true) @CurrentUser Long userId) {
        String nickname = userService.getNickname(userId);
        return ApiResponse.onSuccess(nickname);
    }

    @Operation(summary = "새로운 nickname으로 수정", description = "입력된 닉네임을 중복 닉네임이 있는지 확인하고 수정")
    @PostMapping("/mypage/profile/nickname")
    public ApiResponse<String> updateNickname(@Parameter(hidden = true) @CurrentUser Long userId, @Valid @RequestBody UpdateNicknameRequestDTO request) {
        String newNickname = userService.updateNickname(userId, request.getNickname());
        return ApiResponse.onSuccess(newNickname);
    }

    @Operation(summary = "서비스 알림 동의 변경", description = "서비스 알림 동의가 되어있다면 비활성화, 동의가 안되어있으면 동의로 변경")
    @PatchMapping("/mypage/notification/service")
    public ApiResponse<Boolean> updateServiceNotificationAllow(@Parameter(hidden = true) @CurrentUser Long userId) {
        userService.updateServiceNotificationAllow(userId);
        return ApiResponse.onSuccess(true);
    }

    @Operation(summary = "혜택 및 마케팅 동의 변경", description = "혜택 및 마케팅 알림 동의가 되어있다면 비활성화, 동의가 안되어있으면 동의로 변경")
    @PatchMapping("/mypage/notification/marketing")
    public ApiResponse<Boolean> updateMarketingNotificationAllow(@Parameter(hidden = true) @CurrentUser Long userId) {
        userService.updateMarketingNotificationAllow(userId);
        return ApiResponse.onSuccess(true);
    }

    @Operation(summary = "비밀번호 변경", description = "이메일 인증 토큰으로 비밀번호를 변경한다.")
    @PostMapping("/users/password/change")
    public ApiResponse<Boolean> changePasswordWithToken(@RequestBody PasswordChangeWithTokenRequestDTO request) {
        userService.changePasswordWithToken(request.getToken(), request.getNewPassword());
        return ApiResponse.onSuccess(true);
    }

    @Operation(summary = "유저 정보 조회", description = "유저의 상세 정보 조회")
    @GetMapping("/users/info")
    public ApiResponse<UserInfoResponseDTO> getUserInfo(@Parameter(hidden = true) @CurrentUser Long userId) {
        UserInfoResponseDTO userInfo = userService.getUserInfo(userId);
        return ApiResponse.onSuccess(userInfo);
    }

    @Operation(summary = "이메일 중복 확인", description = "이메일이 이미 사용 중인지 확인합니다")
    @GetMapping("/users/email/check-duplicate")
    public ApiResponse<EmailDuplicateResponseDTO> checkEmailDuplicate(@RequestParam String email) {
        EmailDuplicateResponseDTO response = userService.checkEmailDuplicate(email);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "닉네임 중복 확인", description = "닉네임이 이미 사용 중인지 확인합니다")
    @GetMapping("/users/nickname/check-duplicate")
    public ApiResponse<EmailDuplicateResponseDTO> checkNicknameDuplicate(@RequestParam String nickname) {
        EmailDuplicateResponseDTO response = userService.checkNicknameDuplicate(nickname);
        return ApiResponse.onSuccess(response);
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

    @Operation(summary = "로그아웃", description = "현재 사용자를 로그아웃합니다")
    @PostMapping("/users/logout")
    public ApiResponse<String> logout(
            @Parameter(hidden = true) @CurrentUser Long userId,
            HttpServletRequest httpRequest) {
        
        try {
            userService.logout(userId, httpRequest);
            return ApiResponse.onSuccess("로그아웃되었습니다");
            
        } catch (Exception e) {
            log.error("로그아웃 실패: {}", e.getMessage());
            return ApiResponse.onFailure("LOGOUT_FAILED", e.getMessage(), null);
        }
    }

    @Operation(summary = "토큰 갱신", description = "리프레쉬 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다")
    @PostMapping("/users/refresh")
    public ApiResponse<TokenRefreshResponseDTO> refreshToken(
            @Valid @RequestBody TokenRefreshRequestDTO request) {
        
        try {
            TokenRefreshResponseDTO response = tokenService.refreshAccessToken(
                request.getRefreshToken()
            );
            
            return ApiResponse.onSuccess(response);
            
        } catch (Exception e) {
            log.error("토큰 갱신 실패: {}", e.getMessage());
            return ApiResponse.onFailure("TOKEN_REFRESH_FAILED", e.getMessage(), null);
        }
    }

    @Operation(summary = "토큰 유효성 확인", description = "현재 액세스 토큰의 유효성을 확인합니다")
    @GetMapping("/users/validate")
    public ApiResponse<String> validateToken(
            @Parameter(hidden = true) @CurrentUser Long userId) {
        
        try {
            return ApiResponse.onSuccess("토큰이 유효합니다");
            
        } catch (Exception e) {
            log.error("토큰 유효성 확인 실패: {}", e.getMessage());
            return ApiResponse.onFailure("TOKEN_VALIDATION_FAILED", e.getMessage(), null);
        }
    }

    @Operation(summary = "카카오톡 계정 연동 상태 확인", description = "사용자의 카카오톡 계정 연동 여부와 연동된 이메일을 확인합니다")
    @GetMapping("/mypage/kakao-account")
    public ApiResponse<KakaoAccountResponseDTO> getKakaoAccountStatus(@Parameter(hidden = true) @CurrentUser Long userId) {
        KakaoAccountResponseDTO kakaoAccountStatus = userService.getKakaoAccountStatus(userId);
        return ApiResponse.onSuccess(kakaoAccountStatus);
    }

    @Operation(summary = "프로필 사진 업로드 및 변경", description = "회원가입 시 프로필 사진을 업로드하거나 변경합니다.")
    @PostMapping(value = "/profile/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImageUploadResponseDTO> uploadProfileImage(
            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "회원가입할 이메일 주소", required = true)
            @RequestParam String email) {

        ImageUploadResponseDTO response = userService.uploadProfileImage(file, email);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "마이페이지 프로필 사진 변경", description = "로그인한 사용자의 프로필 사진을 업로드하거나 변경합니다.")
    @PostMapping(value = "/mypage/profile/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImageUploadResponseDTO> updateProfileImage(
            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestPart("file") MultipartFile file,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        ImageUploadResponseDTO response = userService.updateProfileImage(userId, file);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "내 초대코드 조회", description = "내 초대코드를 조회하거나 새로 생성합니다")
    @GetMapping("/users/me/invite-code")
    public ApiResponse<InviteCodeResponseDTO> getMyInviteCode(
            @Parameter(hidden = true) @CurrentUser Long userId) {
        InviteCodeResponseDTO response = userService.getMyInviteCode(userId);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "초대코드 처리", description = "초대코드를 검증하고 친구 관계를 생성합니다")
    @PostMapping("/users/invite-code/process")
    public ApiResponse<InviteCodeProcessResponseDTO> processInviteCode(
            @Valid @RequestBody InviteCodeRequestDTO request,
            @Parameter(hidden = true) @CurrentUser Long userId) {
        InviteCodeProcessResponseDTO response = userService.processInviteCode(request.getInviteCode(), userId);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "초대코드 실시간 검증", description = "입력된 초대코드가 유효한지 실시간으로 검증합니다")
    @PostMapping("/users/invite-code/validate")
    public ApiResponse<ValidateInviteCodeResponseDTO> validateInviteCode(
            @Valid @RequestBody InviteCodeRequestDTO request) {
        ValidateInviteCodeResponseDTO response = userService.validateInviteCode(request.getInviteCode());
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "이메일 인증 발송", description = "이메일 중복 확인 후 인증메일을 발송하고 토큰을 반환합니다")
    @PostMapping("/users/email/send")
    public ApiResponse<EmailSendResponseDTO> sendEmailVerification(@RequestBody @Valid EmailVerificationRequestDTO request) {
        EmailSendResponseDTO response = userService.sendEmailVerification(request.getEmail());
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "이메일 인증 재발송")
    @PostMapping("/users/email/resend")
    public ApiResponse<EmailSendResponseDTO> resendVerificationEmail(@RequestBody @Valid EmailVerificationRequestDTO request) {
        EmailSendResponseDTO response = userService.resendEmailVerification(request.getEmail());
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "이메일 인증 여부 확인", description = "토큰으로 이메일을 확인하고 인증 상태를 반환합니다")
    @GetMapping("/users/email/verification-status")
    public ApiResponse<EmailVerificationStatusResponseDTO> getEmailVerificationStatus(@RequestParam("token") String token) {
        EmailVerificationStatusResponseDTO response = userService.getEmailVerificationStatus(token);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "이메일 링크 클릭 처리", description = "이메일 링크 클릭 시 인증 처리 후 웹페이지 표시")
    @GetMapping("/users/email/verify-link")
    public ResponseEntity<String> handleEmailLink(@RequestParam String token) {
        String html = userService.handleEmailVerificationLink(token);
        return createHtmlResponse(html);
    }

    @Operation(summary = "비밀번호 변경 확인 이메일 발송", description = "비밀번호 변경을 위한 확인 메일을 발송하고 토큰을 반환합니다")
    @PostMapping("/users/password/change-email/send")
    public ApiResponse<EmailSendResponseDTO> sendPasswordChangeEmail(
            @RequestBody @Valid PasswordChangeEmailRequestDTO request) {
        EmailSendResponseDTO response = userService.sendPasswordChangeEmail(
                request.getEmail(), 
                request.getIsLoggedIn() // 로그인 상태 추가
        );
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "비밀번호 변경 확인 이메일 재발송", description = "비밀번호 변경을 위한 확인 메일을 재발송합니다")
    @PostMapping("/users/password/change-email/resend")
    public ApiResponse<EmailSendResponseDTO> resendPasswordChangeEmail(
            @RequestBody @Valid PasswordChangeEmailRequestDTO request) {
        EmailSendResponseDTO response = userService.resendPasswordChangeEmail(
                request.getEmail(), 
                request.getIsLoggedIn()  // 로그인 상태 포함
        );
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "비밀번호 변경 요청 이메일 링크 클릭 처리", description = "비밀번호 변경 링크 클릭 시 확인 처리 후 웹페이지 표시")
    @GetMapping("/users/password/change-link")
    public ResponseEntity<String> handlePasswordChangeLink(@RequestParam String token) {
        String html = userService.handlePasswordChangeLink(token);
        return createHtmlResponse(html);
    }

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 처리하고 탈퇴 이유를 저장합니다")
    @PostMapping("/users/withdraw")
    public ApiResponse<WithdrawalResponseDTO> withdrawUser(
            @Valid @RequestBody WithdrawalRequestDTO request,
            @Parameter(hidden = true) @CurrentUser Long userId) {
        WithdrawalResponseDTO response = userService.withdrawUser(userId, request);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "이메일 변경 인증 메일 발송", description = "새 이메일로 인증 메일을 발송합니다")
    @PostMapping("/users/email/change/send")
    public ApiResponse<EmailSendResponseDTO> sendEmailChangeVerification(
            @RequestBody @Valid EmailVerificationRequestDTO request,
            @Parameter(hidden = true) @CurrentUser Long userId) {
        EmailSendResponseDTO response = userService.sendEmailChangeVerification(userId, request.getEmail());
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "이메일 변경 요청 이메일 링크 클릭 처리", description = "이메일 변경 링크 클릭 시 확인 처리 후 앱으로 리다이렉트")
    @GetMapping("/users/email/change-link")
    public ResponseEntity<String> handleEmailChangeLink(@RequestParam String token) {
        String html = userService.handleEmailChangeLink(token);
        return createHtmlResponse(html);
    }

    @Operation(summary = "이메일 변경 인증 메일 재발송", description = "이메일 변경 인증 메일을 재발송합니다")
    @PostMapping("/users/email/change/resend")
    public ApiResponse<EmailSendResponseDTO> resendEmailChangeVerification(
            @RequestBody @Valid EmailVerificationRequestDTO request,
            @Parameter(hidden = true) @CurrentUser Long userId) {
        EmailSendResponseDTO response = userService.resendEmailChangeVerification(userId, request.getEmail());
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "카카오 소셜 인증",
        description = "카카오 인가코드로 로그인/회원가입 여부를 판단합니다")
    @PostMapping("/users/auth/kakao")
    public ApiResponse<KakaoAuthResponseDTO> kakaoAuth(@Valid @RequestBody KakaoAuthRequestDTO request) {
        KakaoAuthResponseDTO result = userService.kakaoAuth(request);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "카카오 회원가입 완료",
            description = "카카오 온보딩 완료 후 모든 정보를 받아서 회원가입을 완료합니다")
    @PostMapping("/users/auth/kakao/complete")
    public ApiResponse<SignupResponseDTO> kakaoSignupComplete(@Valid @RequestBody KakaoSignupCompleteRequestDTO request) {
        SignupResponseDTO result = userService.kakaoSignupComplete(request);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "카카오 계정 연동",
            description = "기존 계정에 카카오 계정을 연동합니다")
    @PostMapping("/mypage/kakao-account/link")
    public ApiResponse<KakaoLinkResponseDTO> linkKakaoAccount(
            @Valid @RequestBody KakaoLinkRequestDTO request,
            @Parameter(hidden = true) @CurrentUser Long userId) {
        KakaoLinkResponseDTO result = userService.linkKakaoAccount(userId, request);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "이메일 인증 대안 - 카카오 로그인",
            description = "이메일 인증 실패 시 카카오 소셜 로그인으로 전환합니다")
    @PostMapping("/users/auth/email/alternative")
    public ApiResponse<KakaoAuthResponseDTO> emailAuthAlternative(@Valid @RequestBody KakaoAuthRequestDTO request) {
        KakaoAuthResponseDTO result = userService.kakaoAuth(request);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "랜덤 닉네임 생성", description = "형용사+명사 조합으로 랜덤 닉네임을 생성합니다")
    @GetMapping("/profile/nickname/random")
    public ApiResponse<RandomNicknameResponseDTO> generateRandomNickname() {
        RandomNicknameResponseDTO result = randomNicknameService.generateRandomNickname();
        return ApiResponse.onSuccess(result);
    }

    /**
     * HTML 응답을 생성하는 공통 메서드
     * 
     * @param html HTML 문자열
     * @return ResponseEntity with HTML content
     */
    private ResponseEntity<String> createHtmlResponse(String html) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
                .body(html);
    }
}