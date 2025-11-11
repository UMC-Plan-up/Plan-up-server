package com.planup.planup.domain.user.service;

import com.planup.planup.domain.user.dto.*;
import org.springframework.web.multipart.MultipartFile;
import com.planup.planup.domain.user.entity.User;

import java.util.List;

public interface UserService {
    // 사용자 ID로 사용자 조회
    User getUserByUserId(Long userId);

    // 사용자 닉네임 조회
    String getNickname(Long userId);

    // 사용자 닉네임 변경
    String updateNickname(Long userId, String nickname);

    boolean updateMarketingNotificationAllow(Long userId); // 혜택 및 마케팅 알림 변경

    boolean updateServiceNotificationAllow(Long userId); // 서비스 알림 변경


    
    // 비밀번호 변경 이메일 인증 완료 여부 확인
    Boolean isPasswordChangeEmailVerified(String email);

    // 사용자 정보 조회
    UserResponseDTO.UserInfo getUserInfo(Long userId);

    // 이메일 중복 확인 (회원가입용)
    void checkEmail(String email);

    // 이메일 존재 확인 (비밀번호 변경용)
    void checkEmailExists(String email);
    
    // 이메일 사용 가능 여부 확인
    boolean isEmailAvailable(String email);

    // 비밀번호 변경 이메일 발송
    AuthResponseDTO.EmailSend sendPasswordChangeEmail(String email, Boolean isLoggedIn);

    // 비밀번호 변경 이메일 재발송
    AuthResponseDTO.EmailSend resendPasswordChangeEmail(String email, Boolean isLoggedIn);

    // 토큰 기반 비밀번호 변경
    void changePasswordWithToken(String token, String newPassword);

    // 회원가입
    UserResponseDTO.Signup signup(UserRequestDTO.Signup request);

    // 로그인
    UserResponseDTO.Login login(UserRequestDTO.Login request);

    // 카카오 계정 연동 상태 조회
    OAuthResponseDTO.KakaoAccount getKakaoAccountStatus(Long userId);

    // 프로필 이미지 업로드
    FileResponseDTO.ImageUpload uploadProfileImage(MultipartFile file, String email);

    // 마이페이지 프로필 이미지 업데이트
    FileResponseDTO.ImageUpload updateProfileImage(Long userId, MultipartFile file);

    AuthResponseDTO.InviteCode getMyInviteCode(Long userId);

    // 초대코드 처리 및 친구 관계 생성
    AuthResponseDTO.InviteCodeProcess processInviteCode(String inviteCode, Long userId);

    // 초대코드 유효성 검증
    AuthResponseDTO.ValidateInviteCode validateInviteCode(String inviteCode);

    // 회원 탈퇴
    UserResponseDTO.Withdrawal withdrawUser(Long userId, UserRequestDTO.Withdrawal request);

    // 카카오 소셜 인증
    OAuthResponseDTO.KakaoAuth kakaoAuth(OAuthRequestDTO.KakaoAuth request);

    // 카카오 계정 연동
    OAuthResponseDTO.KaKaoLink linkKakaoAccount(Long userId, OAuthRequestDTO.KaKaoLink request);

    // 카카오 회원가입 완료
    UserResponseDTO.Signup kakaoSignupComplete(OAuthRequestDTO.KaKaoSignup request);

    // 이메일 변경
    String updateEmail(Long userId, String newEmail);

    // 이메일 변경 이메일 발송
    AuthResponseDTO.EmailSend sendEmailChangeVerification(String currentEmail, String newEmail);

    // 이메일 변경 완료
    void completeEmailChange(String token);

    // 이메일 변경 인증 메일 재발송
    AuthResponseDTO.EmailSend resendEmailChangeVerification(String currentEmail, String newEmail);

    // 닉네임 중복 확인
    AuthResponseDTO.EmailDuplicate checkNicknameDuplicate(String nickname);

    // 사용자 ID로 친구 목록 조회
    List<User> getFriendsByUserId(Long creatorId);

    // 로그아웃
    void logout(Long userId, jakarta.servlet.http.HttpServletRequest request);

    // 이메일 인증 발송
    AuthResponseDTO.EmailSend sendEmailVerification(String email);

    // 이메일 인증 재발송
    AuthResponseDTO.EmailSend resendEmailVerification(String email);

    // 이메일 인증 상태 확인
    AuthResponseDTO.EmailVerificationStatus getEmailVerificationStatus(String token);

    // 이메일 링크 클릭 처리 (HTML 응답)
    String handleEmailVerificationLink(String token);

    // 비밀번호 변경 링크 클릭 처리 (HTML 응답)
    String handlePasswordChangeLink(String token);

    // 이메일 변경 링크 클릭 처리 (HTML 응답)
    String handleEmailChangeLink(String token);

    // 이메일 중복 확인 (DTO 반환)
    AuthResponseDTO.EmailDuplicate checkEmailDuplicate(String email);

    // 이메일 변경 인증 메일 발송 (userId 기반)
    AuthResponseDTO.EmailSend sendEmailChangeVerification(Long userId, String newEmail);

    // 이메일 변경 인증 메일 재발송 (userId 기반)
    AuthResponseDTO.EmailSend resendEmailChangeVerification(Long userId, String newEmail);
}