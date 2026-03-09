package com.planup.planup.domain.user.converter;

import com.planup.planup.domain.user.dto.AuthRequestDTO;
import com.planup.planup.domain.user.entity.Terms;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.entity.UserTerms;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserTermsConvertor {
    // ======= 약관동의 =======
    public UserTerms toUserTermsEntity(User user, Terms terms, AuthRequestDTO.TermsAgreement agreement) {
        return UserTerms.builder()
                .user(user)
                .terms(terms)
                .isAgreed(agreement.isAgreed())
                .agreedAt(agreement.isAgreed() ? LocalDateTime.now() : null)
                .build();
    }
}
