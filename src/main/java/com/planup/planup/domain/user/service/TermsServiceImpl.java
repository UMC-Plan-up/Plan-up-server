package com.planup.planup.domain.user.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.user.converter.TermsConverter;
import com.planup.planup.domain.user.dto.AuthResponseDTO;
import com.planup.planup.domain.user.entity.Terms;
import com.planup.planup.domain.user.repository.TermsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TermsServiceImpl implements TermsService {

    private final TermsRepository termsRepository;

    @Override
    @Transactional
    public List<AuthResponseDTO.TermsList> getTermsList() {
        List<Terms> termsList = termsRepository.findAllByOrderByOrderAsc();

        return TermsConverter.toTermsListResponseList(termsList);
    }

    @Override
    @Transactional
    public AuthResponseDTO.TermsDetail getTermsDetail(Long termsId) {
        Terms terms = termsRepository.findById(termsId)
                .orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_TERMS));

        return TermsConverter.toTermsDetailResponse(terms);
    }

}
