package com.planup.planup.domain.user.service.command;

import com.planup.planup.domain.user.dto.AuthRequestDTO;
import com.planup.planup.domain.user.entity.User;

import java.util.List;

public interface UserTermsService {
    void validateRequiredTerms(List<AuthRequestDTO.TermsAgreement> agreements);

    void addTermsAgreements(User user, List<AuthRequestDTO.TermsAgreement> agreements);
}
