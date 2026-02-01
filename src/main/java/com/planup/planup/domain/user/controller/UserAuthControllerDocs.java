package com.planup.planup.domain.user.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.user.dto.*;
import com.planup.planup.validation.jwt.dto.TokenRefreshRequestDTO;
import com.planup.planup.validation.jwt.dto.TokenRefreshResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "User Auth Controller", description = "유저 인증/인가, 소셜 로그인 및 계정 관리 API")
public interface UserAuthControllerDocs {

    // ======================== 기본 인증 (가입/로그인/토큰) ========================

    @Operation(summary = "회원가입", description = "이메일/비밀번호로 새 계정을 생성합니다.")
    ApiResponse<UserResponseDTO.Signup> signup(@Valid @RequestBody UserRequestDTO.Signup request);

    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    ApiResponse<UserResponseDTO.Login> login(@Valid @RequestBody UserRequestDTO.Login request);

    @Operation(summary = "로그아웃", description = "현재 사용자를 로그아웃합니다.")
    ApiResponse<String> logout(@Parameter(hidden = true) Long userId, HttpServletRequest httpRequest);

    @Operation(summary = "토큰 갱신", description = "리프레쉬 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.")
    ApiResponse<TokenRefreshResponseDTO> refreshToken(@Valid @RequestBody TokenRefreshRequestDTO request);

    @Operation(summary = "토큰 유효성 확인", description = "현재 액세스 토큰의 유효성을 확인합니다.")
    ApiResponse<String> validateToken(@Parameter(hidden = true) Long userId);

    // ======================== 계정 검증 및 복구 (이메일/비밀번호) ========================

    @Operation(summary = "이메일 중복 확인", description = "이메일이 이미 사용 중인지 확인합니다.")
    ApiResponse<AuthResponseDTO.EmailDuplicate> checkEmailDuplicate(@RequestParam String email);

    @Operation(summary = "비밀번호 변경", description = "현재 유저의 비밀번호를 변경합니다.")
    ApiResponse<Boolean> changePasswordWithToken(@RequestBody UserRequestDTO.PasswordChangeWithToken request, @Parameter(hidden = true) Long userId);

    @Operation(summary = "이메일 인증 발송", description = "이메일 중복 확인 후 인증메일을 발송하고 토큰을 반환합니다.")
    ApiResponse<AuthResponseDTO.EmailSend> sendEmailVerification(@RequestBody @Valid AuthRequestDTO.EmailVerification request);

    @Operation(summary = "이메일 인증 재발송", description = "기존 이메일로 인증 메일을 재발송합니다.")
    ApiResponse<AuthResponseDTO.EmailSend> resendVerificationEmail(@RequestBody @Valid AuthRequestDTO.EmailVerification request);

    @Operation(summary = "이메일 인증 여부 확인", description = "토큰으로 이메일을 확인하고 인증 상태를 반환합니다.")
    ApiResponse<AuthResponseDTO.EmailVerificationStatus> getEmailVerificationStatus(@RequestParam("token") String token);

    @Operation(summary = "이메일 링크 클릭 처리", description = "이메일 링크 클릭 시 인증 처리 후 웹페이지(HTML)를 반환합니다.")
    ResponseEntity<String> handleEmailLink(@RequestParam String token);

    @Operation(summary = "비밀번호 변경 확인 이메일 발송", description = "비밀번호 변경을 위한 확인 메일을 발송하고 토큰을 반환합니다.")
    ApiResponse<AuthResponseDTO.EmailSend> sendPasswordChangeEmail(@RequestBody @Valid UserRequestDTO.PasswordChangeEmail request);

    @Operation(summary = "비밀번호 변경 확인 이메일 재발송", description = "비밀번호 변경을 위한 확인 메일을 재발송합니다.")
    ApiResponse<AuthResponseDTO.EmailSend> resendPasswordChangeEmail(@RequestBody @Valid UserRequestDTO.PasswordChangeEmail request);

    @Operation(summary = "비밀번호 변경 요청 이메일 링크 클릭 처리", description = "비밀번호 변경 링크 클릭 시 확인 처리 후 웹페이지(HTML)를 반환합니다.")
    ResponseEntity<String> handlePasswordChangeLink(@RequestParam String token);

    // ======================== 소셜 인증 및 연동 (핵심 변경 사항) ========================

    @Operation(summary = "카카오 소셜 인증", description = "카카오 인가코드로 로그인 또는 회원가입 여부를 판단합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @ExampleObject(name = "1. 로그인 성공 (기존 유저)",
                                            description = "이미 가입된 유저입니다. 액세스 토큰과 유저 정보를 반환합니다.",
                                            value = """
                                                {
                                                  "isSuccess": true,
                                                  "code": "COMMON200",
                                                  "message": "성공입니다.",
                                                  "result": {
                                                    "tempUserId": null,
                                                    "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJyb...",
                                                    "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJ1c...",
                                                    "expiresIn": 3600,
                                                    "userInfo": {
                                                      "id": 46,
                                                      "email": "test@planup.com",
                                                      "nickname": "테스트유저",
                                                      "profileImg": "string",
                                                      "serviceNotificationAllow": true,
                                                      "marketingNotificationAllow": true
                                                    },
                                                    "newUser": false
                                                  }
                                                }
                                                """
                                    ),
                                    @ExampleObject(name = "2. 회원가입 필요 (신규 유저)",
                                            description = "가입되지 않은 유저입니다. tempUserId를 반환하며, 이를 이용해 회원가입 완료 API를 호출해야 합니다.",
                                            value = """
                                                {
                                                  "isSuccess": true,
                                                  "code": "COMMON200",
                                                  "message": "성공입니다.",
                                                  "result": {
                                                    "tempUserId": "4423fc15-ad4d-45b8-a153-81c50f3fe223",
                                                    "accessToken": null,
                                                    "refreshToken": null,
                                                    "expiresIn": null,
                                                    "userInfo": null,
                                                    "newUser": true
                                                  }
                                                }
                                                """
                                    )
                            }
                    )
            )
    })
    ApiResponse<OAuthResponseDTO.KakaoAuth> kakaoAuth(@Valid @RequestBody OAuthRequestDTO.KakaoAuth request);

    @Operation(summary = "카카오 회원가입 완료", description = "카카오 온보딩 완료 후 모든 정보를 받아서 회원가입을 완료합니다.")
    ApiResponse<UserResponseDTO.Signup> kakaoSignupComplete(@Valid @RequestBody OAuthRequestDTO.KaKaoSignup request);

    @Operation(summary = "이메일 인증 대안 - 카카오 로그인", description = "이메일 인증 실패 시 카카오 소셜 로그인으로 전환합니다.")
    ApiResponse<OAuthResponseDTO.KakaoAuth> emailAuthAlternative(@Valid @RequestBody OAuthRequestDTO.KakaoAuth request);

    @Operation(summary = "카카오 계정 연동 여부 조회", description = "현재 로그인한 사용자의 카카오 계정 연동 여부를 확인합니다.")
    ApiResponse<OAuthResponseDTO.KakaoLinkStatus> getKakaoLinkStatus(@Parameter(hidden = true) Long userId);

    // ======================== 초대 코드 처리 ========================

    @Operation(summary = "내 초대코드 조회", description = "내 초대코드를 조회하거나 새로 생성합니다.")
    ApiResponse<AuthResponseDTO.InviteCode> getMyInviteCode(@Parameter(hidden = true) Long userId);

    @Operation(summary = "초대코드 처리", description = "초대코드를 검증하고 친구 관계를 생성합니다.")
    ApiResponse<AuthResponseDTO.InviteCodeProcess> processInviteCode(@Valid @RequestBody AuthRequestDTO.InviteCode request, @Parameter(hidden = true) Long userId);

    @Operation(summary = "초대코드 실시간 검증", description = "입력된 초대코드가 유효한지 실시간으로 검증합니다.")
    ApiResponse<AuthResponseDTO.ValidateInviteCode> validateInviteCode(@Valid @RequestBody AuthRequestDTO.InviteCode request);

    // ======================== 회원 탈퇴 ========================

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 처리하고 탈퇴 이유를 저장합니다.")
    ApiResponse<UserResponseDTO.Withdrawal> withdrawUser(@Valid @RequestBody UserRequestDTO.Withdrawal request, @Parameter(hidden = true) Long userId);
}