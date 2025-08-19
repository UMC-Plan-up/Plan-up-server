package com.planup.planup.domain.user.service;

import com.planup.planup.domain.user.dto.*;
import org.springframework.web.multipart.MultipartFile;
import com.planup.planup.domain.user.entity.User;

public interface UserService {
    User getUserbyUserId(Long userId);

    String getNickname(Long userId);

    String updateNickname(Long userId, String nickname);

    boolean updateNotificationAgree(Long userId);


    
    // 비밀번호 변경 이메일 인증 완료 여부 확인
    Boolean isPasswordChangeEmailVerified(String email);

    UserInfoResponseDTO getUserInfo(Long userId);

    void checkEmail(String email);

    // 이메일 존재 확인 (비밀번호 변경용)
    void checkEmailExists(String email);
    
    // 이메일 사용 가능 여부 확인
    boolean isEmailAvailable(String email);

    // 비밀번호 변경 이메일 발송
    EmailSendResponseDTO sendPasswordChangeEmail(String email);

    // 비밀번호 변경 이메일 재발송
    EmailSendResponseDTO resendPasswordChangeEmail(String email);

    // 토큰 기반 비밀번호 변경
    void changePasswordWithToken(String token, String newPassword);

    SignupResponseDTO signup(SignupRequestDTO request);

    LoginResponseDTO login(LoginRequestDTO request);

    KakaoAccountResponseDTO getKakaoAccountStatus(Long userId);

    ImageUploadResponseDTO uploadProfileImage(MultipartFile file, User currentUser);

    InviteCodeResponseDTO getMyInviteCode(Long userId);

    InviteCodeProcessResponseDTO processInviteCode(String inviteCode, Long userId);

    ValidateInviteCodeResponseDTO validateInviteCode(String inviteCode);

    WithdrawalResponseDTO withdrawUser(Long userId, WithdrawalRequestDTO request);

    KakaoAuthResponseDTO kakaoAuth(KakaoAuthRequestDTO request);

    SignupResponseDTO kakaoSignupComplete(KakaoSignupCompleteRequestDTO request);

    String updateEmail(Long userId, String newEmail);

    // 이메일 변경 이메일 발송
    EmailSendResponseDTO sendEmailChangeVerification(String currentEmail, String newEmail);

    // 이메일 변경 완료
    void completeEmailChange(String token);

    // 이메일 변경 인증 메일 재발송
    EmailSendResponseDTO resendEmailChangeVerification(String currentEmail, String newEmail);

    // 닉네임 중복 확인
    EmailDuplicateResponseDTO checkNicknameDuplicate(String nickname);
}