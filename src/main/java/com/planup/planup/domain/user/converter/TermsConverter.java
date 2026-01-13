package com.planup.planup.domain.user.converter;

import com.planup.planup.domain.user.dto.AuthResponseDTO;
import com.planup.planup.domain.user.entity.Terms;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TermsConverter {

    /**
     * Terms 엔티티를 약관 목록 응답 DTO로 변환
     */
    public static AuthResponseDTO.TermsList toTermsListResponse(Terms terms) {
        return AuthResponseDTO.TermsList.builder()
                .id(terms.getId())
                .summary(terms.getSummary())
                .isRequired(terms.getIsRequired())
                .order(terms.getOrder())
                .build();
    }

    /**
     * Terms 엔티티를 약관 상세 응답 DTO로 변환
     */
    public static AuthResponseDTO.TermsDetail toTermsDetailResponse(Terms terms) {
        return AuthResponseDTO.TermsDetail.builder()
                .id(terms.getId())
                .content(terms.getContent())
                .build();
    }

    /**
     * Terms 엔티티 리스트를 약관 목록 응답 DTO 리스트로 변환
     */
    public static List<AuthResponseDTO.TermsList> toTermsListResponseList(List<Terms> termsList) {
        return termsList.stream()
                .map(TermsConverter::toTermsListResponse)
                .collect(Collectors.toList());
    }
}