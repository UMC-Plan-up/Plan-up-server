package com.planup.planup.domain.user.converter;

import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.friend.entity.FriendStatus;
import com.planup.planup.domain.oauth.entity.AuthProvideerEnum;
import com.planup.planup.domain.oauth.entity.OAuthAccount;
import com.planup.planup.domain.user.dto.*;
import com.planup.planup.domain.user.dto.external.KakaoUserInfo;
import com.planup.planup.domain.user.entity.Terms;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.entity.UserTerms;
import com.planup.planup.domain.user.entity.UserWithdrawal;
import com.planup.planup.domain.user.enums.Role;
import com.planup.planup.domain.user.enums.TokenStatus;
import com.planup.planup.domain.user.enums.UserActivate;
import com.planup.planup.domain.user.enums.UserLevel;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserAuthConverter {

    // ========= User 관련 =========
    /**
     * User 엔티티를 회원가입 응답 DTO로 변환
     */
    public UserResponseDTO.Signup toSignupResponseDTO(User user, String accessToken, String refreshToken, Long expiresIn) {
        return UserResponseDTO.Signup.builder()
                .id(user.getId())
                .email(user.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .build();
    }

    /**
     * User 엔티티를 사용자 정보 응답 DTO로 변환
     */
    public UserResponseDTO.UserInfo toUserInfoResponseDTO(User user) {
        return UserResponseDTO.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImg(user.getProfileImg())
                .build();
    }

    /**
     * 회원가입 요청 DTO를 User 엔티티로 변환
     */
    public User toUserEntity(UserRequestDTO.Signup request, String encodedPassword, String profileImgUrl) {
        return User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .role(Role.USER)
                .userActivate(UserActivate.ACTIVE)
                .userLevel(UserLevel.LEVEL_1)
                .serviceNotificationAllow(true)
                .marketingNotificationAllow(true)
                .profileImg(profileImgUrl)
                .emailVerified(true)
                .emailVerifiedAt(LocalDateTime.now())
                .build();
    }

    /**
     * User 엔티티를 로그인 응답 DTO로 변환
     */
    public UserResponseDTO.Login toLoginResponseDTO(User user, String accessToken) {
        return UserResponseDTO.Login.builder()
                .accessToken(accessToken)
                .nickname(user.getNickname())
                .profileImgUrl(user.getProfileImg())
                .build();
    }

    // ======= 회원탈퇴 =======
    /**
     * 회원 탈퇴 응답 DTO 생성
     */
    public UserResponseDTO.Withdrawal toWithdrawalResponseDTO(boolean success, String message, String withdrawalDate) {
        return UserResponseDTO.Withdrawal.builder()
                .success(success)
                .message(message)
                .withdrawalDate(withdrawalDate)
                .build();
    }

    /**
     * 회원 탈퇴 엔티티 생성
     */
    public UserWithdrawal toUserWithdrawalEntity(User user, String reason) {
        return UserWithdrawal.builder()
                .user(user)
                .reason(reason)
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }

    // ========= 초대코드 =========
    /**
     * 초대 코드 처리 응답 DTO 생성
     */
    public AuthResponseDTO.InviteCodeProcess toInviteCodeProcessResponseDTO(boolean success, String friendNickname, String message) {
        return AuthResponseDTO.InviteCodeProcess.builder()
                .success(success)
                .friendNickname(friendNickname)
                .message(message)
                .build();
    }

    /**
     * 초대 코드 검증 응답 DTO 생성
     */
    public AuthResponseDTO.ValidateInviteCode toValidateInviteCodeResponseDTO(boolean valid, String targetUserNickname) {
        String message = valid ? "유효한 초대코드입니다." : "유효하지 않은 초대코드입니다.";

        return AuthResponseDTO.ValidateInviteCode.builder()
                .valid(valid)
                .message(message)
                .targetUserNickname(targetUserNickname)
                .build();
    }

    /**
     * 친구 관계 엔티티 생성
     */
    public Friend toFriendEntity(User user, User friend, FriendStatus status) {
        return Friend.builder()
                .user(user)
                .friend(friend)
                .status(status)
                .build();
    }

    // ========= 이메일 인증 =========
    /**
     * 이메일 발송 응답 DTO 생성
     */
    public AuthResponseDTO.EmailSend toEmailSendResponseDTO(String email, String verificationToken, String message) {
        return AuthResponseDTO.EmailSend.builder()
                .email(email)
                .message(message)
                .verificationToken(verificationToken)
                .build();
    }

    /**
     * 이메일 중복 확인 응답 DTO 생성
     */
    public AuthResponseDTO.EmailDuplicate toEmailDuplicateResponseDTO(boolean isAvailable, String message) {
        return AuthResponseDTO.EmailDuplicate.builder()
                .available(isAvailable)
                .message(message)
                .build();
    }

    /**
     * 이메일 인증 상태 응답 DTO 생성
     */
    public AuthResponseDTO.EmailVerificationStatus toEmailVerificationStatusResponseDTO(String email, boolean verified) {
        if (email != null) {
            return AuthResponseDTO.EmailVerificationStatus.builder()
                    .verified(verified)
                    .email(email)
                    .tokenStatus(TokenStatus.VALID)
                    .build();
        } else {
            return AuthResponseDTO.EmailVerificationStatus.builder()
                    .verified(verified)
                    .email(null)
                    .tokenStatus(TokenStatus.EXPIRED_OR_INVALID)
                    .build();
        }
    }

    // ======= 약관동의 =======
    /**
     * 약관 동의 엔티티 생성
     */
    public UserTerms toUserTermsEntity(User user, Terms terms, AuthRequestDTO.TermsAgreement agreement) {
        return UserTerms.builder()
                .user(user)
                .terms(terms)
                .isAgreed(agreement.isAgreed())
                .agreedAt(agreement.isAgreed() ? LocalDateTime.now() : null)
                .build();
    }

    // ======= OAuth/카카오 =======
    /**
     * OAuth 계정 엔티티 생성
     */
    public OAuthAccount toOAuthAccountEntity(User user, String email, AuthProvideerEnum provider) {
        return OAuthAccount.builder()
                .provider(provider)
                .email(email)
                .user(user)
                .build();
    }

    /**
     * 카카오 연동 응답 DTO 생성
     */
    public OAuthResponseDTO.KaKaoLink toKakaoLinkResponseDTO(boolean success, String message, String kakaoEmail, UserResponseDTO.UserInfo userInfo) {
        return OAuthResponseDTO.KaKaoLink.builder()
                .success(success)
                .message(message)
                .kakaoEmail(kakaoEmail)
                .userInfo(userInfo)
                .build();
    }

    /**
     * 카카오 인증 응답 DTO 생성 (기존 사용자)
     */
    public OAuthResponseDTO.KakaoAuth toKakaoAuthResponseDTO(boolean isNewUser, String accessToken, String refreshToken, Long expiresIn, UserResponseDTO.UserInfo userInfo) {
        return OAuthResponseDTO.KakaoAuth.builder()
                .isNewUser(isNewUser)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .userInfo(userInfo)
                .build();
    }

    /**
     * 카카오 인증 응답 DTO 생성 (신규 사용자)
     */
    public OAuthResponseDTO.KakaoAuth toKakaoAuthResponseDTO(boolean isNewUser, String tempUserId) {
        return OAuthResponseDTO.KakaoAuth.builder()
                .isNewUser(isNewUser)
                .tempUserId(tempUserId)
                .build();
    }

    /**
     * 카카오 회원가입용 User 엔티티 생성
     */
    public User toKakaoUserEntity(KakaoUserInfo kakaoUserInfo, OAuthRequestDTO.KaKaoSignup request) {
        return User.builder()
                .email(kakaoUserInfo.getKakaoAccount().getEmail())
                .password(null)
                .nickname(request.getNickname())
                .role(Role.USER)
                .userActivate(UserActivate.ACTIVE)
                .userLevel(UserLevel.LEVEL_1)
                .serviceNotificationAllow(true)
                .marketingNotificationAllow(true)
                .profileImg(request.getProfileImg())
                .emailVerified(true)
                .emailVerifiedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 카카오 계정 상태 응답 DTO 생성
     */
    public OAuthResponseDTO.KakaoAccount toKakaoAccountResponseDTO(boolean isLinked, String kakaoEmail) {
        return OAuthResponseDTO.KakaoAccount.builder()
                .isLinked(isLinked)
                .kakaoEmail(kakaoEmail)
                .build();
    }

    /**
     * 카카오 계정 연동 여부 응답 DTO 생성
     */
    public OAuthResponseDTO.KakaoLinkStatus toKakaoLinkStatusResponseDTO(boolean isLinked) {
        return OAuthResponseDTO.KakaoLinkStatus.builder()
                .isKakaoLinked(isLinked)
                .build();
    }
}