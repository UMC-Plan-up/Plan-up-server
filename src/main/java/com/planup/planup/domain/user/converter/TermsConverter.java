package com.planup.planup.domain.user.converter;

import com.planup.planup.domain.user.dto.TermsDetailResponseDTO;
import com.planup.planup.domain.user.dto.TermsListResponseDTO;
import com.planup.planup.domain.user.entity.Terms;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TermsConverter {

    // Terms → TermsListResponseDTO 변환
    public static TermsListResponseDTO toTermsListResponse(Terms terms) {
        return TermsListResponseDTO.builder()
                .id(terms.getId())
                .summary(terms.getSummary())
                .isRequired(terms.getIsRequired())
                .order(terms.getOrder())
                .build();
    }

    // Terms → TermsDetailResponseDTO 변환
    public static TermsDetailResponseDTO toTermsDetailResponse(Terms terms) {
        return TermsDetailResponseDTO.builder()
                .id(terms.getId())
                .content(terms.getContent())
                .build();
    }

    // List<Terms> → List<TermsListResponseDTO> 변환
    public static List<TermsListResponseDTO> toTermsListResponseList(List<Terms> termsList) {
        return termsList.stream()
                .map(TermsConverter::toTermsListResponse)
                .collect(Collectors.toList());
    }
}
