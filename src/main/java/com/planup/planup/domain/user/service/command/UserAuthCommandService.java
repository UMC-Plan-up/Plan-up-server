package com.planup.planup.domain.user.service.command;

import com.planup.planup.domain.user.dto.*;
import jakarta.servlet.http.HttpServletRequest;

public interface UserAuthCommandService {
    // 회원가입/로그인
    UserResponseDTO.Signup signup(UserRequestDTO.Signup request);
    UserResponseDTO.Login login(UserRequestDTO.Login request);
    void logout(Long userId, HttpServletRequest request);
    UserResponseDTO.Withdrawal withdrawUser(Long userId, UserRequestDTO.Withdrawal request);

    // 카카오 OAuth
    OAuthResponseDTO.KakaoAuth kakaoAuth(OAuthRequestDTO.KakaoAuth request);
    UserResponseDTO.Signup kakaoSignupComplete(OAuthRequestDTO.KaKaoSignup request);
    OAuthResponseDTO.KaKaoLink linkKakaoAccount(Long userId, OAuthRequestDTO.KaKaoLink request);

    // 초대 코드
    AuthResponseDTO.InviteCodeProcess processInviteCode(String inviteCode, Long userId);

    // 이메일 인증 (회원가입용)
    AuthResponseDTO.EmailSend sendEmailVerification(String email);
    AuthResponseDTO.EmailSend resendEmailVerification(String email);
    String handleEmailVerificationLink(String token);

    // 비밀번호 변경
    void changePasswordWithToken(String token, String newPassword);
    AuthResponseDTO.EmailSend sendPasswordChangeEmail(String email, Boolean isLoggedIn);
    AuthResponseDTO.EmailSend resendPasswordChangeEmail(String email, Boolean isLoggedIn);
    String handlePasswordChangeLink(String token);
    void markPasswordChangeEmailAsVerified(String email);
}
