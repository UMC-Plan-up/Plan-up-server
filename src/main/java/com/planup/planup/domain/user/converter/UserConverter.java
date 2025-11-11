package com.planup.planup.domain.user.converter;

import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.friend.entity.FriendStatus;
import com.planup.planup.domain.oauth.entity.AuthProvideerEnum;
import com.planup.planup.domain.oauth.entity.OAuthAccount;
import com.planup.planup.domain.user.dto.*;
import com.planup.planup.domain.user.dto.external.KakaoUserInfo;
import com.planup.planup.domain.user.entity.*;
import com.planup.planup.domain.user.enums.Role;
import com.planup.planup.domain.user.enums.TokenStatus;
import com.planup.planup.domain.user.enums.UserActivate;
import com.planup.planup.domain.user.enums.UserLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class UserConverter {

    /*// SignupRequestDTO를 User 엔티티로 변환
    public User toUserEntity(SignupRequestDTO request, String encodedPassword, Map<Long, Terms> termsMap) {
        User user = User.builder()
                    .email(request.getEmail())
                    .password(encodedPassword)
                    .nickname(request.getNickname())
                    .role(Role.USER)
                    .userActivate(UserActivate.ACTIVE)
                    .userLevel(UserLevel.LEVEL_1)
                    .serviceNotificationAllow(true) // 서비스 알림 기본값: true
                    .marketingNotificationAllow(true) // 혜택 및 마케팅 알림 기본값: true
                    .profileImg(request.getProfileImg())
                    .build();

        // 약관 동의 정보도 함께 생성
        for (TermsAgreementRequestDTO agreement : request.getAgreements()) {
            Terms terms = termsMap.get(agreement.getTermsId());

            UserTerms userTerms = UserTerms.builder()
                    .user(user)
                    .terms(terms)
                    .isAgreed(agreement.isAgreed())
                    .agreedAt(agreement.isAgreed() ? LocalDateTime.now() : null)
                    .build();

            user.getUserTermList().add(userTerms);
        }

        return user;
    }
*/
    // User 엔티티와 accessToken을 SignupResponseDTO로 변환
    public UserResponseDTO.Signup toSignupResponseDTO(User user, String accessToken) {
        return UserResponseDTO.Signup.builder()
                .id(user.getId())
                .email(user.getEmail())
                .accessToken(accessToken)
                .build();
    }

    // User 엔티티를 UserInfoResponseDTO로 변환
    public UserResponseDTO.UserInfo toUserInfoResponseDTO(User user) {
        return UserResponseDTO.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImg(user.getProfileImg())
                .build();
    }

    // User 엔티티를 LoginResponseDTO로 변환
    public UserResponseDTO.Login toLoginResponseDTO(User user, String accessToken) {
        return UserResponseDTO.Login.builder()
                .accessToken(accessToken)
                .nickname(user.getNickname())
                .profileImgUrl(user.getProfileImg())
                .build();
    }

    // TermsAgreementRequestDTO와 관련 엔티티들을 UserTerms로 변환
    public UserTerms toUserTermsEntity(User user, Terms terms, AuthRequestDTO.TermsAgreement agreement) {
        return UserTerms.builder()
                .user(user)
                .terms(terms)
                .isAgreed(agreement.isAgreed())
                .agreedAt(agreement.isAgreed() ? LocalDateTime.now() : null)
                .build();
    }

    // 비밀번호 변경 확인 메일 발송 응답 DTO로 변환
    public AuthResponseDTO.EmailSend toEmailSendResponseDTO(String email, String verificationToken) {
        return AuthResponseDTO.EmailSend.builder()
                .email(email)
                .message("비밀번호 변경 확인 메일이 발송되었습니다")
                .verificationToken(verificationToken)
                .build();
    }

    // 이메일 발송 응답 DTO로 변환 (메시지 커스터마이징 가능)
    public AuthResponseDTO.EmailSend toEmailSendResponseDTO(String email, String verificationToken, String message) {
        return AuthResponseDTO.EmailSend.builder()
                .email(email)
                .message(message)
                .verificationToken(verificationToken)
                .build();
    }

    // 이메일 인증 상태 응답 DTO로 변환
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

    // EmailDuplicateResponseDTO 생성
    public AuthResponseDTO.EmailDuplicate toEmailDuplicateResponseDTO(boolean isAvailable, String message) {
        return AuthResponseDTO.EmailDuplicate.builder()
                .available(isAvailable)
                .message(message)
                .build();
    }

    // KakaoAccountResponseDTO 생성
    public OAuthResponseDTO.KakaoAccount toKakaoAccountResponseDTO(boolean isLinked, String kakaoEmail) {
        return OAuthResponseDTO.KakaoAccount.builder()
                .isLinked(isLinked)
                .kakaoEmail(kakaoEmail)
                .build();
    }

    // ImageUploadResponseDTO 생성
    public FileResponseDTO.ImageUpload toImageUploadResponseDTO(String imageUrl) {
        return FileResponseDTO.ImageUpload.builder()
                .imageUrl(imageUrl)
                .build();
    }

    // InviteCodeProcessResponseDTO 생성
    public AuthResponseDTO.InviteCodeProcess toInviteCodeProcessResponseDTO(boolean success, String friendNickname, String message) {
        return AuthResponseDTO.InviteCodeProcess.builder()
                .success(success)
                .friendNickname(friendNickname)
                .message(message)
                .build();
    }

    // ValidateInviteCodeResponseDTO 생성
    public AuthResponseDTO.ValidateInviteCode toValidateInviteCodeResponseDTO(boolean valid, String message, String targetUserNickname) {
        return AuthResponseDTO.ValidateInviteCode.builder()
                .valid(valid)
                .message(message)
                .targetUserNickname(targetUserNickname)
                .build();
    }

    // WithdrawalResponseDTO 생성
    public UserResponseDTO.Withdrawal toWithdrawalResponseDTO(boolean success, String message, String withdrawalDate) {
        return UserResponseDTO.Withdrawal.builder()
                .success(success)
                .message(message)
                .withdrawalDate(withdrawalDate)
                .build();
    }

    // KakaoLinkResponseDTO 생성
    public OAuthResponseDTO.KaKaoLink toKakaoLinkResponseDTO(boolean success, String message, String kakaoEmail, UserResponseDTO.UserInfo userInfo) {
        return OAuthResponseDTO.KaKaoLink.builder()
                .success(success)
                .message(message)
                .kakaoEmail(kakaoEmail)
                .userInfo(userInfo)
                .build();
    }

    // KakaoAuthResponseDTO 생성 (기존 사용자)
    public OAuthResponseDTO.KakaoAuth toKakaoAuthResponseDTO(boolean isNewUser, String accessToken, UserResponseDTO.UserInfo userInfo) {
        return OAuthResponseDTO.KakaoAuth.builder()
                .isNewUser(isNewUser)
                .accessToken(accessToken)
                .userInfo(userInfo)
                .build();
    }

    // KakaoAuthResponseDTO 생성 (신규 사용자)
    public OAuthResponseDTO.KakaoAuth toKakaoAuthResponseDTO(boolean isNewUser, String tempUserId) {
        return OAuthResponseDTO.KakaoAuth.builder()
                .isNewUser(isNewUser)
                .tempUserId(tempUserId)
                .build();
    }

    // SignupRequestDTO를 User 엔티티로 변환 (프로필 이미지 URL 포함)
    public User toUserEntity(UserRequestDTO.Signup request, String encodedPassword, String profileImgUrl) {
        return User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .role(Role.USER)
                .userActivate(UserActivate.ACTIVE)
                .userLevel(UserLevel.LEVEL_1)
                .serviceNotificationAllow(true) // 서비스 알림 기본값: true
                .marketingNotificationAllow(true) // 혜택 및 마케팅 알림 기본값: true
                .profileImg(profileImgUrl)
                .emailVerified(true)
                .emailVerifiedAt(LocalDateTime.now())
                .build();
    }

    // KakaoSignupCompleteRequestDTO와 KakaoUserInfo를 User 엔티티로 변환
    public User toUserEntityFromKakao(KakaoUserInfo kakaoUserInfo, OAuthRequestDTO.KaKaoSignup request, String profileImgUrl) {
        return User.builder()
                .email(kakaoUserInfo.getKakaoAccount().getEmail())
                .password(null) // 카카오는 비밀번호 없음
                .nickname(request.getNickname())
                .role(Role.USER)
                .userActivate(UserActivate.ACTIVE)
                .userLevel(UserLevel.LEVEL_1)
                .serviceNotificationAllow(true) // 서비스 알림 기본값: true
                .marketingNotificationAllow(true) // 혜택 및 마케팅 알림 기본값: true
                .profileImg(profileImgUrl)
                .emailVerified(true)
                .emailVerifiedAt(LocalDateTime.now())
                .build();
    }

    // OAuthAccount 엔티티 생성
    public OAuthAccount toOAuthAccountEntity(User user, String email, AuthProvideerEnum provider) {
        return OAuthAccount.builder()
                .provider(provider)
                .email(email)
                .user(user)
                .build();
    }

    // Friend 엔티티 생성
    public Friend toFriendEntity(User user, User friend, FriendStatus status) {
        return Friend.builder()
                .user(user)
                .friend(friend)
                .status(status)
                .build();
    }

    // UserWithdrawal 엔티티 생성
    public UserWithdrawal toUserWithdrawalEntity(User user, String reason) {
        return UserWithdrawal.builder()
                .user(user)
                .reason(reason)
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
}
