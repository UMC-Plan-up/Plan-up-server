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
import com.planup.planup.domain.user.enums.*;
import com.planup.planup.domain.user.dto.UserResponseDTO.UserInfo;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserAuthConverter {

    public UserResponseDTO.AuthResponseDTO toAuthResponseDTO(User user, UserStatus status, String accessToken, String refreshToken, Long expiresIn) {
        return UserResponseDTO.AuthResponseDTO.builder()
                .userStatus(status)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .userInfo(toUserInfo(user))
                .build();
    }

    public UserResponseDTO.AuthResponseDTO toAuthResponseDTO(UserStatus status, UserInfo userInfo) {
        return UserResponseDTO.AuthResponseDTO.builder()
                .userStatus(status)
                .userInfo(userInfo)
                .build();
    }

    public UserResponseDTO.AuthResponseDTO toAuthResponseDTO(UserStatus status, String tempUserId, KakaoUserInfo kakaoUserInfo) {
        return UserResponseDTO.AuthResponseDTO.builder()
                .userStatus(status)
                .tempUserId(tempUserId)
                .userInfo(toUserInfo(kakaoUserInfo))
                .build();
    }

    public UserInfo toUserInfo(User user) {
        if (user == null) return null;
        return UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .birthDate(user.getBirthDate())
                .gender(user.getGender())
                .profileImg(user.getProfileImg())
                .serviceNotificationAllow(user.getServiceNotificationAllow())
                .marketingNotificationAllow(user.getMarketingNotificationAllow())
                .build();
    }

    public UserInfo toUserInfo(KakaoUserInfo kakaoUserInfo) {
        if (kakaoUserInfo == null) return null;

        KakaoUserInfo.KakaoAccount account = kakaoUserInfo.getKakaoAccount();
        KakaoUserInfo.Properties properties = kakaoUserInfo.getProperties();

        Gender gender = Gender.UNKNOWN;
        if (account != null && account.getGender() != null) {
            String genderStr = account.getGender().toLowerCase();
            if ("male".equals(genderStr)) {
                gender = Gender.MALE;
            } else if ("female".equals(genderStr)) {
                gender = Gender.FEMALE;
            }
        }

        return UserInfo.builder()
                .email(account != null ? account.getEmail() : null)
                .name(properties != null ? properties.getNickname() : null) // 카카오 닉네임을 이름으로 사용
                .gender(gender)
                .profileImg(properties != null ? properties.getProfileImageUrl() : null)
                .build();
    }

    /**
     * 일반 회원가입 요청 DTO를 User 엔티티로 변환
     */
    public User toUserEntity(UserRequestDTO.Signup request, String encodedPassword, String profileImgUrl) {
        return User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .name(request.getName())
                .birthDate(request.getBirthDate())
                .role(Role.USER)
                .gender(request.getGender())
                .userActivate(UserActivate.ACTIVE)
                .userLevel(UserLevel.LEVEL_1)
                .socialType(AuthProvideerEnum.EMAIL)
                .serviceNotificationAllow(true)
                .marketingNotificationAllow(true)
                .profileImg(profileImgUrl)
                .emailVerified(true)
                .emailVerifiedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 카카오 회원가입용 DTO를 User 엔티티로 변환
     */
    public User toKakaoUserEntity(KakaoUserInfo kakaoUserInfo, OAuthRequestDTO.KaKaoSignup signupRequest) {
        Gender gender = Gender.UNKNOWN;
        if (kakaoUserInfo.getKakaoAccount() != null && kakaoUserInfo.getKakaoAccount().getGender() != null) {
            String genderStr = kakaoUserInfo.getKakaoAccount().getGender().toLowerCase();
            if ("male".equals(genderStr)) {
                gender = Gender.MALE;
            } else if ("female".equals(genderStr)) {
                gender = Gender.FEMALE;
            }
        }

        return User.builder()
                .email(kakaoUserInfo.getKakaoAccount().getEmail())
                .password(null) // 카카오 로그인은 비밀번호 없음
                .nickname(signupRequest.getNickname())
                .name(signupRequest.getName())
                .birthDate(signupRequest.getBirthDate())
                .role(Role.USER)
                .gender(gender)
                .userActivate(UserActivate.ACTIVE)
                .userLevel(UserLevel.LEVEL_1)
                .socialType(AuthProvideerEnum.KAKAO)
                .serviceNotificationAllow(true)
                .marketingNotificationAllow(true)
                .profileImg(signupRequest.getProfileImg())
                .emailVerified(true)
                .emailVerifiedAt(LocalDateTime.now())
                .build();
    }


    // ======= 회원탈퇴 =======
    public UserResponseDTO.Withdrawal toWithdrawalResponseDTO(boolean success, String message, String withdrawalDate) {
        return UserResponseDTO.Withdrawal.builder()
                .success(success)
                .message(message)
                .withdrawalDate(withdrawalDate)
                .build();
    }

    public UserWithdrawal toUserWithdrawalEntity(User user, String reason) {
        return UserWithdrawal.builder()
                .userId(user.getId())
                .reason(reason)
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }

    // ========= 초대코드 =========
    public AuthResponseDTO.InviteCodeProcess toInviteCodeProcessResponseDTO(boolean success, String friendNickname, String message) {
        return AuthResponseDTO.InviteCodeProcess.builder()
                .success(success)
                .friendNickname(friendNickname)
                .message(message)
                .build();
    }

    public AuthResponseDTO.ValidateInviteCode toValidateInviteCodeResponseDTO(boolean valid, String targetUserNickname) {
        String message = valid ? "유효한 초대코드입니다." : "유효하지 않은 초대코드입니다.";
        return AuthResponseDTO.ValidateInviteCode.builder()
                .valid(valid)
                .message(message)
                .targetUserNickname(targetUserNickname)
                .build();
    }

    public Friend toFriendEntity(User user, User friend, FriendStatus status) {
        return Friend.builder()
                .user(user)
                .friend(friend)
                .status(status)
                .build();
    }

    // ========= 이메일 인증 =========
    public AuthResponseDTO.EmailSend toEmailSendResponseDTO(String email, String verificationToken, String message) {
        return AuthResponseDTO.EmailSend.builder()
                .email(email)
                .message(message)
                .verificationToken(verificationToken)
                .build();
    }

    public AuthResponseDTO.EmailDuplicate toEmailDuplicateResponseDTO(boolean isAvailable, String message) {
        return AuthResponseDTO.EmailDuplicate.builder()
                .available(isAvailable)
                .message(message)
                .build();
    }

    public AuthResponseDTO.EmailVerificationStatus toEmailVerificationStatusResponseDTO(String email, boolean verified, TokenStatus tokenStatus) {
        return AuthResponseDTO.EmailVerificationStatus.builder()
                .verified(verified)
                .email(email)
                .tokenStatus(tokenStatus)
                .build();
    }

    // ======= OAuth/카카오 =======
    public OAuthAccount toOAuthAccountEntity(User user, String email, AuthProvideerEnum provider) {
        return OAuthAccount.builder()
                .provider(provider)
                .email(email)
                .user(user)
                .build();
    }

    public OAuthResponseDTO.KaKaoLink toKakaoLinkResponseDTO(boolean success, String message, String kakaoEmail, UserInfo userInfo) {
        return OAuthResponseDTO.KaKaoLink.builder()
                .success(success)
                .message(message)
                .kakaoEmail(kakaoEmail)
                .userInfo(userInfo)
                .build();
    }

    public OAuthResponseDTO.KakaoLinkStatus toKakaoLinkStatusResponseDTO(boolean isLinked) {
        return OAuthResponseDTO.KakaoLinkStatus.builder()
                .isKakaoLinked(isLinked)
                .build();
    }

    public OAuthResponseDTO.KakaoAccount toKakaoAccountResponseDTO(boolean isLinked, String kakaoEmail) {
        return OAuthResponseDTO.KakaoAccount.builder()
                .isLinked(isLinked)
                .kakaoEmail(kakaoEmail)
                .build();
    }
}
