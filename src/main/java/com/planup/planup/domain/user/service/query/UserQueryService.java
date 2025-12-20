package com.planup.planup.domain.user.service.query;

import com.planup.planup.domain.user.dto.AuthResponseDTO;
import com.planup.planup.domain.user.dto.OAuthResponseDTO;
import com.planup.planup.domain.user.dto.UserResponseDTO;
import com.planup.planup.domain.user.entity.User;

import java.util.List;

public interface UserQueryService {
    // 기본 정보 조회
    User getUserByUserId(Long userId);
    String getNickname(Long userId);
    UserResponseDTO.UserInfo getUserInfo(Long userId);

    // 이메일 검증
    void checkEmail(String email);  // 회원가입용 중복 체크
    void checkEmailExists(String email);  // 비밀번호 변경용 존재 확인
    boolean isEmailAvailable(String email);
    AuthResponseDTO.EmailDuplicate checkEmailDuplicate(String email);

    // 닉네임
    AuthResponseDTO.EmailDuplicate checkNicknameDuplicate(String nickname);
    UserResponseDTO.RandomNickname generateRandomNickname();

    // 인증 상태 조회
    OAuthResponseDTO.KakaoAccount getKakaoAccountStatus(Long userId);
    Boolean isEmailVerified(String email);
    Boolean isPasswordChangeEmailVerified(String email);
    AuthResponseDTO.EmailVerificationStatus getEmailVerificationStatus(String token);
    OAuthResponseDTO.KakaoLinkStatus getKakaoLinkStatus(Long userId);

    // 관계 조회
    List<User> getFriendsByUserId(Long userId);
    AuthResponseDTO.InviteCode getMyInviteCode(Long userId);
    AuthResponseDTO.ValidateInviteCode validateInviteCode(String inviteCode);

    // 약관 조회
    List<AuthResponseDTO.TermsList> getTermsList();
    AuthResponseDTO.TermsDetail getTermsDetail(Long termsId);
}
