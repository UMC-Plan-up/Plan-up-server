package com.planup.planup.domain.user.converter;

import com.planup.planup.domain.user.dto.EmailSendResponseDTO;
import com.planup.planup.domain.user.dto.EmailVerificationStatusResponseDTO;
import com.planup.planup.domain.user.dto.LoginResponseDTO;
import com.planup.planup.domain.user.dto.SignupRequestDTO;
import com.planup.planup.domain.user.dto.SignupResponseDTO;
import com.planup.planup.domain.user.dto.TermsAgreementRequestDTO;
import com.planup.planup.domain.user.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
public class UserConverter {

    // SignupRequestDTO를 User 엔티티로 변환
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

    // User 엔티티를 SignupResponseDTO로 변환
    public SignupResponseDTO toSignupResponseDTO(User user) {
        return SignupResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();
    }

    // User 엔티티를 LoginResponseDTO로 변환
    public LoginResponseDTO toLoginResponseDTO(User user, String accessToken) {
        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .nickname(user.getNickname())
                .profileImgUrl(user.getProfileImg())
                .build();
    }

    // TermsAgreementRequestDTO와 관련 엔티티들을 UserTerms로 변환
    public UserTerms toUserTermsEntity(User user, Terms terms, TermsAgreementRequestDTO agreement) {
        return UserTerms.builder()
                .user(user)
                .terms(terms)
                .isAgreed(agreement.isAgreed())
                .agreedAt(agreement.isAgreed() ? LocalDateTime.now() : null)
                .build();
    }

    // 비밀번호 변경 확인 메일 발송 응답 DTO로 변환
    public EmailSendResponseDTO toEmailSendResponseDTO(String email, String verificationToken) {
        return EmailSendResponseDTO.builder()
                .email(email)
                .message("비밀번호 변경 확인 메일이 발송되었습니다")
                .verificationToken(verificationToken)
                .build();
    }

    // 이메일 발송 응답 DTO로 변환 (메시지 커스터마이징 가능)
    public EmailSendResponseDTO toEmailSendResponseDTO(String email, String verificationToken, String message) {
        return EmailSendResponseDTO.builder()
                .email(email)
                .message(message)
                .verificationToken(verificationToken)
                .build();
    }

    // 이메일 인증 상태 응답 DTO로 변환
    public EmailVerificationStatusResponseDTO toEmailVerificationStatusResponseDTO(String email, boolean verified) {
        if (email != null) {
            return EmailVerificationStatusResponseDTO.builder()
                    .verified(verified)
                    .email(email)
                    .tokenStatus(TokenStatus.VALID)
                    .build();
        } else {
            return EmailVerificationStatusResponseDTO.builder()
                    .verified(verified)
                    .email(null)
                    .tokenStatus(TokenStatus.EXPIRED_OR_INVALID)
                    .build();
        }
    }
}
