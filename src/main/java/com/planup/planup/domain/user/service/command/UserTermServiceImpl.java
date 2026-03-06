package com.planup.planup.domain.user.service.command;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.notification.service.NotificationPreferenceService;
import com.planup.planup.domain.user.converter.UserTermsConvertor;
import com.planup.planup.domain.user.dto.AuthRequestDTO;
import com.planup.planup.domain.user.entity.Terms;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.entity.UserTerms;
import com.planup.planup.domain.user.repository.TermsRepository;
import com.planup.planup.domain.user.repository.UserTermsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserTermServiceImpl implements UserTermsService {

    private final TermsRepository termsRepository;
    private final UserTermsRepository userTermsRepository;
    private final UserTermsConvertor userTermsConvertor;
    private final NotificationPreferenceService notificationPreferenceService;


    @Override
    public void validateRequiredTerms(List<AuthRequestDTO.TermsAgreement> agreements) {
        if (agreements == null) {
            throw new UserException(ErrorStatus.REQUIRED_TERMS_NOT_AGREED);
        }
        List<Terms> requiredTerms = termsRepository.findByIsRequiredTrue();

        Set<Long> agreedTermsIds = agreements.stream()
                .filter(AuthRequestDTO.TermsAgreement::isAgreed)
                .map(AuthRequestDTO.TermsAgreement::getTermsId)
                .collect(Collectors.toSet());

        for (Terms requiredTerm : requiredTerms) {
            if (!agreedTermsIds.contains(requiredTerm.getId())) {
                throw new UserException(ErrorStatus.REQUIRED_TERMS_NOT_AGREED);
            }
        }
    }

    @Override
    public void addTermsAgreements(User user, List<AuthRequestDTO.TermsAgreement> agreements) {
        if (agreements == null) return;
        List<Long> termsIds = agreements.stream()
                .map(AuthRequestDTO.TermsAgreement::getTermsId)
                .toList();

        List<Terms> foundTerms = termsRepository.findAllById(termsIds);

        if (foundTerms.size() != termsIds.size()) {
            throw new UserException(ErrorStatus.NOT_FOUND_TERMS);
        }

        Map<Long, Terms> termsMap = foundTerms.stream()
                .collect(Collectors.toMap(Terms::getId, terms -> terms));

        List<UserTerms> userTermsList = agreements.stream()
                .map(agreement -> {
                    Terms terms = termsMap.get(agreement.getTermsId());
                    return userTermsConvertor.toUserTermsEntity(user, terms, agreement);
                })
                .toList();

        userTermsRepository.saveAll(userTermsList);
        notificationPreferenceService.addNotificationPreference(foundTerms, user);
    }
}
