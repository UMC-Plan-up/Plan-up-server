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

    // 혜택 및 마케팅 알림 동의 상태 변경
    boolean updateNotificationAgree(Long userId);
    
    // 비밀번호 변경 이메일 인증 완료 여부 확인
    Boolean isPasswordChangeEmailVerified(String email);

    // 사용자 정보 조회
    UserInfoResponseDTO getUserInfo(Long userId);

    // 이메일 중복 확인 (회원가입용)
    void checkEmail(String email);

    // 이메일 존재 확인 (비밀번호 변경용)
    void checkEmailExists(String email);
    
    // 이메일 사용 가능 여부 확인
    boolean isEmailAvailable(String email);

    // 비밀번호 변경 이메일 발송
    EmailSendResponseDTO sendPasswordChangeEmail(String email, Boolean isLoggedIn);

    // 비밀번호 변경 이메일 재발송
    EmailSendResponseDTO resendPasswordChangeEmail(String email, Boolean isLoggedIn);

    // 토큰 기반 비밀번호 변경
    void changePasswordWithToken(String token, String newPassword);

    // 회원가입
    SignupResponseDTO signup(SignupRequestDTO request);

    // 로그인
    LoginResponseDTO login(LoginRequestDTO request);

    // 카카오 계정 연동 상태 조회
    KakaoAccountResponseDTO getKakaoAccountStatus(Long userId);

    // 프로필 이미지 업로드
    ImageUploadResponseDTO uploadProfileImage(MultipartFile file, String email);

    // 내 초대코드 조회
    InviteCodeResponseDTO getMyInviteCode(Long userId);

    // 초대코드 처리 및 친구 관계 생성
    InviteCodeProcessResponseDTO processInviteCode(String inviteCode, Long userId);

    // 초대코드 유효성 검증
    ValidateInviteCodeResponseDTO validateInviteCode(String inviteCode);

    // 회원 탈퇴
    WithdrawalResponseDTO withdrawUser(Long userId, WithdrawalRequestDTO request);

    // 카카오 소셜 인증
    KakaoAuthResponseDTO kakaoAuth(KakaoAuthRequestDTO request);

    // 카카오 계정 연동
    KakaoLinkResponseDTO linkKakaoAccount(Long userId, KakaoLinkRequestDTO request);

    // 카카오 회원가입 완료
    SignupResponseDTO kakaoSignupComplete(KakaoSignupCompleteRequestDTO request);

    // 이메일 변경
    String updateEmail(Long userId, String newEmail);

    // 이메일 변경 이메일 발송
    EmailSendResponseDTO sendEmailChangeVerification(String currentEmail, String newEmail);

    // 이메일 변경 완료
    void completeEmailChange(String token);

    // 이메일 변경 인증 메일 재발송
    EmailSendResponseDTO resendEmailChangeVerification(String currentEmail, String newEmail);

    // 닉네임 중복 확인
    EmailDuplicateResponseDTO checkNicknameDuplicate(String nickname);

    // 사용자 ID로 친구 목록 조회
    List<User> getFriendsByUserId(Long creatorId);

    // 이메일 인증 발송
    EmailSendResponseDTO sendEmailVerification(String email);

    // 이메일 인증 재발송
    EmailSendResponseDTO resendEmailVerification(String email);

    // 이메일 인증 상태 확인
    EmailVerificationStatusResponseDTO getEmailVerificationStatus(String token);

    // 이메일 링크 클릭 처리 (HTML 응답)
    String handleEmailVerificationLink(String token);

    // 비밀번호 변경 링크 클릭 처리 (HTML 응답)
    String handlePasswordChangeLink(String token);

    // 이메일 변경 링크 클릭 처리 (HTML 응답)
    String handleEmailChangeLink(String token);

    // 이메일 중복 확인 (DTO 반환)
    EmailDuplicateResponseDTO checkEmailDuplicate(String email);

    // 이메일 변경 인증 메일 발송 (userId 기반)
    EmailSendResponseDTO sendEmailChangeVerification(Long userId, String newEmail);

    // 이메일 변경 인증 메일 재발송 (userId 기반)
    EmailSendResponseDTO resendEmailChangeVerification(Long userId, String newEmail);
}