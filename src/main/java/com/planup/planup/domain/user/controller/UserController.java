package com.planup.planup.domain.user.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.user.converter.UserConverter;
import com.planup.planup.domain.user.dto.*;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.EmailService;
import com.planup.planup.domain.user.service.RandomNicknameService;
import com.planup.planup.domain.user.service.UserService;
import com.planup.planup.validation.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final EmailService emailService;
    private final UserConverter userConverter;
    private final RandomNicknameService randomNicknameService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Operation(summary = "nickname 변경 요청", description = "닉네임을 변경하기 위해 기존 닉네임 호출")
    @GetMapping("/mypage/profile/nickname")
    public ApiResponse<String> updateNicknameReq(@Parameter(hidden = true) @CurrentUser Long userId) {
        String nickname = userService.getNickname(userId);
        return ApiResponse.onSuccess(nickname);
    }

    @Operation(summary = "새로운 nickname으로 수정", description = "입력된 닉네임을 중복 닉네임이 있는지 확인하고 수정")
    @PostMapping("/mypage/profile/nickname")
    public ApiResponse<String> updateNickname(@Parameter(hidden = true) @CurrentUser Long userId, @RequestBody String nickname) {
        String newNickname = userService.updateNickname(userId, nickname);
        return ApiResponse.onSuccess(newNickname);
    }

    @Operation(summary = "혜택 및 마케팅 동의 변경", description = "혜택 동의가 되어있다면 비활성화, 동의가 안되어있으면 동의로 변경")
    @PatchMapping("/mypage/notification/agree")
    public ApiResponse<Boolean> updateNotificationAgree(@Parameter(hidden = true) @CurrentUser Long userId) {
        userService.updateNotificationAgree(userId);
        return ApiResponse.onSuccess(true);
    }



    @Operation(summary = "비밀번호 변경", description = "이메일 인증 토큰으로 비밀번호를 변경한다.")
    @PostMapping("/users/password/change")
    public ApiResponse<Boolean> changePasswordWithToken(@RequestBody PasswordChangeWithTokenRequestDTO request) {
        try {
            userService.changePasswordWithToken(request.getToken(), request.getNewPassword());
            return ApiResponse.onSuccess(true);
        } catch (IllegalArgumentException e) {
            return ApiResponse.onFailure("PASSWORD4002", e.getMessage(), false);
        } catch (Exception e) {
            log.error("비밀번호 변경 실패: {}", e.getMessage());
            return ApiResponse.onFailure("PASSWORD4003", "비밀번호 변경에 실패했습니다.", false);
        }
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
        boolean isAvailable = userService.isEmailAvailable(email);
        
        EmailDuplicateResponseDTO response = EmailDuplicateResponseDTO.builder()
                .available(isAvailable)
                .message(isAvailable ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.")
                .build();
        
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

    @Operation(summary = "로그아웃", description = "현재 세션을 종료합니다")
    @PostMapping("/users/logout")
    public ApiResponse<String> logout() {
        // 간단한 로그아웃 구현 (JWT는 클라이언트에서 삭제)
        return ApiResponse.onSuccess("로그아웃이 완료되었습니다");
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

    @Operation(summary = "초대코드 처리", description = "초대코드를 검증하고 친구 관계를 생성합니다")
    @PostMapping("/users/invite-code/process")
    public ApiResponse<InviteCodeProcessResponseDTO> processInviteCode(
            @Valid @RequestBody InviteCodeProcessRequestDTO request,
            @Parameter(hidden = true) @CurrentUser User currentUser) {
        InviteCodeProcessResponseDTO response = userService.processInviteCode(request.getInviteCode(), currentUser.getId());
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "초대코드 실시간 검증", description = "입력된 초대코드가 유효한지 실시간으로 검증합니다")
    @PostMapping("/users/invite-code/validate")
    public ApiResponse<ValidateInviteCodeResponseDTO> validateInviteCode(
            @Valid @RequestBody ValidateInviteCodeRequestDTO request) {
        ValidateInviteCodeResponseDTO response = userService.validateInviteCode(request.getInviteCode());
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "이메일 인증 발송", description = "이메일 중복 확인 후 인증메일을 발송하고 토큰을 반환합니다")
    @PostMapping("/users/email/send")
    public ApiResponse<EmailSendResponseDTO> sendEmailVerification(@RequestBody @Valid EmailVerificationRequestDTO request) {
        userService.checkEmail(request.getEmail());

        String verificationToken = emailService.sendVerificationEmail(request.getEmail());

        EmailSendResponseDTO response = userConverter.toEmailSendResponseDTO(request.getEmail(), verificationToken, "인증 메일이 발송되었습니다");

        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "이메일 인증 재발송")
    @PostMapping("/users/email/resend")
    public ApiResponse<EmailSendResponseDTO> resendVerificationEmail (@RequestBody @Valid EmailVerificationRequestDTO request) {

        if (emailService.isEmailVerified(request.getEmail())) {
            throw new UserException(ErrorStatus. EMAIL_ALREADY_VERIFIED);
        }
        // 인증메일 재발송
        String verificationToken = emailService.resendVerificationEmail(request.getEmail());

        EmailSendResponseDTO response = userConverter.toEmailSendResponseDTO(request.getEmail(), verificationToken, "인증 메일이 재발송되었습니다");

        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "이메일 링크 클릭 처리", description = "이메일 링크 클릭 시 인증 처리 후 웹페이지 표시")
    @GetMapping("/users/email/verify-link")
    public ResponseEntity<String> handleEmailLink(@RequestParam String token) {
        try {
            String email = emailService.completeVerification(token);

            String deepLinkUrl = "planup://profile/setup?email=" +
                    URLEncoder.encode(email, StandardCharsets.UTF_8) +
                    "&verified=true&token=" + token +
                    "&from=email_verification";

            String html = emailService.createSuccessHtml(email, deepLinkUrl);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
                    .body(html);

        } catch (IllegalArgumentException e) {
            log.error("이메일 인증 실패 - 토큰: {}, 오류: {}", token, e.getMessage());
            String html = emailService.createFailureHtml();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
                    .body(html);
        } catch (Exception e) {
            log.error("이메일 인증 처리 중 예상치 못한 오류 - 토큰: {}, 오류: {}", token, e.getMessage());
            String html = emailService.createFailureHtml();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
                    .body(html);
        }
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
    public ApiResponse<EmailSendResponseDTO> resendPasswordChangeEmail(@RequestBody @Valid ResendEmailRequestDTO request) {
        EmailSendResponseDTO response = userService.resendPasswordChangeEmail(request.getEmail());
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "비밀번호 변경 요청 이메일 링크 클릭 처리", description = "비밀번호 변경 링크 클릭 시 확인 처리 후 웹페이지 표시")
    @GetMapping("/users/password/change-link")
    public ResponseEntity<String> handlePasswordChangeLink(@RequestParam String token) {
        try {
            String email = emailService.validatePasswordChangeToken(token);
            
            // 비밀번호 변경 이메일 인증 완료 표시
            emailService.markPasswordChangeEmailAsVerified(email);
            
            String deepLinkUrl = "planup://password/change?email=" +
                    java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8) +
                    "&verified=true&token=" + token +
                    "&from=password_change";

            String html = emailService.createSuccessHtml(email, deepLinkUrl);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
                    .body(html);

        } catch (IllegalArgumentException e) {
            String html = emailService.createFailureHtml();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
                    .body(html);
        }
    }

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 처리하고 탈퇴 이유를 저장합니다")
    @PostMapping("/users/withdraw")
    public ApiResponse<WithdrawalResponseDTO> withdrawUser(
            @Valid @RequestBody WithdrawalRequestDTO request,
            @Parameter(hidden = true) @CurrentUser User currentUser) {
        WithdrawalResponseDTO response = userService.withdrawUser(currentUser.getId(), request);
        return ApiResponse.onSuccess(response);
    }


    @Operation(summary = "이메일 변경 인증 메일 발송", description = "새 이메일로 인증 메일을 발송합니다")
    @PostMapping("/users/email/change/send")
    public ApiResponse<EmailSendResponseDTO> sendEmailChangeVerification(
            @RequestBody @Valid EmailVerificationRequestDTO request,  // EmailChangeRequestDTO 대신
            @Parameter(hidden = true) @CurrentUser Long userId) {
        
        User currentUser = userService.getUserbyUserId(userId);
        EmailSendResponseDTO response = userService.sendEmailChangeVerification(
                currentUser.getEmail(), request.getEmail());  // .getNewEmail() 대신 .getEmail()
        
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "이메일 변경 요청 이메일 링크 클릭 처리", description = "이메일 변경 링크 클릭 시 확인 처리 후 앱으로 리다이렉트")
    @GetMapping("/users/email/change-link")
    public ResponseEntity<String> handleEmailChangeLink(@RequestParam String token) {
        try {
            EmailVerifyLinkResponseDTO response = emailService.handleEmailChangeLink(token);
            
            if (response.isVerified()) {
                // 실제 이메일 변경 실행
                userService.completeEmailChange(token);
                
                // 성공 시 HTML 페이지 표시
                String deepLinkUrl = "planup://email/change/complete?verified=true&token=" + token;
                String html = emailService.createSuccessHtml(response.getEmail(), deepLinkUrl);
                
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
                        .body(html);
            } else {
                // 실패 시 에러 HTML 페이지 표시
                String html = emailService.createFailureHtml();
                
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
                        .body(html);
            }
        } catch (Exception e) {
            // 예외 발생 시 에러 HTML 페이지 표시
            String html = emailService.createFailureHtml();
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
                    .body(html);
        }
    }

    @Operation(summary = "이메일 변경 인증 메일 재발송", description = "이메일 변경 인증 메일을 재발송합니다")
    @PostMapping("/users/email/change/resend")
    public ApiResponse<EmailSendResponseDTO> resendEmailChangeVerification(
            @RequestBody @Valid EmailVerificationRequestDTO request,
            @Parameter(hidden = true) @CurrentUser Long userId) {
        
        User currentUser = userService.getUserbyUserId(userId);
        EmailSendResponseDTO response = userService.resendEmailChangeVerification(
                currentUser.getEmail(), request.getEmail());
        
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
}