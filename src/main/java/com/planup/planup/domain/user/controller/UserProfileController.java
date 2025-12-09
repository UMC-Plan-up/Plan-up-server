package com.planup.planup.domain.user.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.user.dto.*;
import com.planup.planup.domain.user.service.command.UserAuthCommandService;
import com.planup.planup.domain.user.service.command.UserProfileCommandService;
import com.planup.planup.domain.user.service.query.UserQueryService;
import com.planup.planup.validation.annotation.CurrentUser;
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
import com.planup.planup.domain.user.dto.OAuthRequestDTO;
import com.planup.planup.domain.user.dto.OAuthResponseDTO;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    private final UserAuthCommandService userAuthCommandService;
    private final UserProfileCommandService userProfileCommandService;
    private final UserQueryService userQueryService;

    // HTML 응답을 생성하는 공통 메서드
    private ResponseEntity<String> createHtmlResponse(String html) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
                .body(html);
    }

    // ======================== 유저 정보 조회 및 닉네임 ========================

    @Operation(summary = "유저 정보 조회", description = "유저의 상세 정보 조회")
    @GetMapping("/users/info")
    public ApiResponse<UserResponseDTO.UserInfo> getUserInfo(@Parameter(hidden = true) @CurrentUser Long userId) {
        UserResponseDTO.UserInfo userInfo = userQueryService.getUserInfo(userId);
        return ApiResponse.onSuccess(userInfo);
    }

    @Operation(summary = "닉네임 중복 확인", description = "닉네임이 이미 사용 중인지 확인합니다")
    @GetMapping("/users/nickname/check-duplicate")
    public ApiResponse<AuthResponseDTO.EmailDuplicate> checkNicknameDuplicate(@RequestParam String nickname) {
        AuthResponseDTO.EmailDuplicate response = userQueryService.checkNicknameDuplicate(nickname);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "랜덤 닉네임 생성", description = "형용사+명사 조합으로 랜덤 닉네임을 생성합니다")
    @GetMapping("/profile/nickname/random")
    public ApiResponse<UserResponseDTO.RandomNickname> generateRandomNickname() {
        UserResponseDTO.RandomNickname result = userQueryService.generateRandomNickname();
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "nickname 변경 요청", description = "닉네임을 변경하기 위해 기존 닉네임 호출")
    @GetMapping("/mypage/profile/nickname")
    public ApiResponse<String> updateNicknameReq(@Parameter(hidden = true) @CurrentUser Long userId) {
        String nickname = userQueryService.getNickname(userId);
        return ApiResponse.onSuccess(nickname);
    }

    @Operation(summary = "새로운 nickname으로 수정", description = "입력된 닉네임을 중복 닉네임이 있는지 확인하고 수정")
    @PostMapping("/mypage/profile/nickname")
    public ApiResponse<String> updateNickname(@Parameter(hidden = true) @CurrentUser Long userId, @Valid @RequestBody UserRequestDTO.UpdateNickname request) {
        String newNickname = userProfileCommandService.updateNickname(userId, request.getNickname());
        return ApiResponse.onSuccess(newNickname);
    }

    // ======================== 프로필 이미지 ========================

    @Operation(summary = "프로필 사진 업로드 및 변경", description = "회원가입 시 프로필 사진을 업로드하거나 변경합니다.")
    @PostMapping(value = "/profile/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileResponseDTO.ImageUpload> uploadProfileImage(
            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "회원가입할 이메일 주소", required = true)
            @RequestParam String email) {

        FileResponseDTO.ImageUpload response = userProfileCommandService.uploadProfileImage(file, email);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "마이페이지 프로필 사진 변경", description = "로그인한 사용자의 프로필 사진을 업로드하거나 변경합니다.")
    @PostMapping(value = "/mypage/profile/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileResponseDTO.ImageUpload> updateProfileImage(
            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestPart("file") MultipartFile file,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        FileResponseDTO.ImageUpload response = userProfileCommandService.updateProfileImage(userId, file);
        return ApiResponse.onSuccess(response);
    }

    // ======================== 알림 설정 ========================

    @Operation(summary = "서비스 알림 동의 변경", description = "서비스 알림 동의가 되어있다면 비활성화, 동의가 안되어있으면 동의로 변경")
    @PatchMapping("/mypage/notification/service")
    public ApiResponse<Boolean> updateServiceNotificationAllow(@Parameter(hidden = true) @CurrentUser Long userId) {
        boolean result = userProfileCommandService.updateServiceNotificationAllow(userId);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "혜택 및 마케팅 동의 변경", description = "혜택 및 마케팅 알림 동의가 되어있다면 비활성화, 동의가 안되어있으면 동의로 변경")
    @PatchMapping("/mypage/notification/marketing")
    public ApiResponse<Boolean> updateMarketingNotificationAllow(@Parameter(hidden = true) @CurrentUser Long userId) {
        boolean result = userProfileCommandService.updateMarketingNotificationAllow(userId);
        return ApiResponse.onSuccess(result);
    }

    // ======================== 계정 연동 및 변경 ========================

    @Operation(summary = "카카오톡 계정 연동 상태 확인", description = "사용자의 카카오톡 계정 연동 여부와 연동된 이메일을 확인합니다")
    @GetMapping("/mypage/kakao-account")
    public ApiResponse<OAuthResponseDTO.KakaoAccount> getKakaoAccountStatus(@Parameter(hidden = true) @CurrentUser Long userId) {
        OAuthResponseDTO.KakaoAccount kakaoAccountStatus = userQueryService.getKakaoAccountStatus(userId);
        return ApiResponse.onSuccess(kakaoAccountStatus);
    }

    @Operation(summary = "카카오 계정 연동", description = "기존 계정에 카카오 계정을 연동합니다")
    @PostMapping("/mypage/kakao-account/link")
    public ApiResponse<OAuthResponseDTO.KaKaoLink> linkKakaoAccount(
            @Valid @RequestBody OAuthRequestDTO.KaKaoLink request,
            @Parameter(hidden = true) @CurrentUser Long userId) {
        OAuthResponseDTO.KaKaoLink result = userAuthCommandService.linkKakaoAccount(userId, request);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "이메일 변경 인증 메일 발송", description = "새 이메일로 인증 메일을 발송합니다")
    @PostMapping("/users/email/change/send")
    public ApiResponse<AuthResponseDTO.EmailSend> sendEmailChangeVerification(
            @RequestBody @Valid AuthRequestDTO.EmailVerification request,
            @Parameter(hidden = true) @CurrentUser Long userId) {
        AuthResponseDTO.EmailSend response = userProfileCommandService.sendEmailChangeVerification(userId, request.getEmail());
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "이메일 변경 요청 이메일 링크 클릭 처리", description = "이메일 변경 링크 클릭 시 확인 처리 후 앱으로 리다이렉트")
    @GetMapping("/users/email/change-link")
    public ResponseEntity<String> handleEmailChangeLink(@RequestParam String token) {
        String html = userProfileCommandService.handleEmailChangeLink(token);
        return createHtmlResponse(html);
    }

    @Operation(summary = "이메일 변경 인증 메일 재발송", description = "이메일 변경 인증 메일을 재발송합니다")
    @PostMapping("/users/email/change/resend")
    public ApiResponse<AuthResponseDTO.EmailSend> resendEmailChangeVerification(
            @RequestBody @Valid AuthRequestDTO.EmailVerification request,
            @Parameter(hidden = true) @CurrentUser Long userId) {
        AuthResponseDTO.EmailSend response = userProfileCommandService .resendEmailChangeVerification(userId, request.getEmail());
        return ApiResponse.onSuccess(response);
    }
}