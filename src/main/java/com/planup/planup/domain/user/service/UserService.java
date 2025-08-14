package com.planup.planup.domain.user.service;

import com.planup.planup.domain.user.dto.*;
import org.springframework.web.multipart.MultipartFile;
import com.planup.planup.domain.user.entity.User;

public interface UserService {
    User getUserbyUserId(Long userId);

    String getNickname(Long userId);

    String updateNickname(Long userId, String nickname);

    boolean updateNotificationAgree(Long userId);

    boolean checkPassword(Long userId, String password);

    void updatePassword(Long userId, String password);

    UserInfoResponseDTO getUserInfo(Long userId);

    void checkEmail(String email);

    // 이메일 존재 확인 (비밀번호 변경용)
    void checkEmailExists(String email);

    // 비밀번호 변경 이메일 발송
    EmailSendResponseDTO sendPasswordChangeEmail(String email);

    // 비밀번호 변경 이메일 재발송
    EmailSendResponseDTO resendPasswordChangeEmail(String email);


    SignupResponseDTO signup(SignupRequestDTO request);

    LoginResponseDTO login(LoginRequestDTO request);

    KakaoAccountResponseDTO getKakaoAccountStatus(Long userId);

    ImageUploadResponseDTO uploadProfileImage(MultipartFile file, User currentUser);

    InviteCodeResponseDTO getMyInviteCode(Long userId);

    ValidateInviteCodeResponseDTO validateInviteCode(String inviteCode, Long currentUserId);

    WithdrawalResponseDTO withdrawUser(Long userId, WithdrawalRequestDTO request);

    KakaoAuthResponseDTO kakaoAuth(KakaoAuthRequestDTO request);

    SignupResponseDTO kakaoSignupComplete(KakaoSignupCompleteRequestDTO request);

    KakaoAuthResponseDTO kakaoAuth(KakaoAuthRequestDTO request);

    SignupResponseDTO kakaoSignupComplete(KakaoSignupCompleteRequestDTO request);

    String updateEmail(Long userId, String newEmail);
}