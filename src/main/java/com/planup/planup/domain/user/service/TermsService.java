package com.planup.planup.domain.user.service;

import com.planup.planup.domain.user.dto.TermsAgreementRequestDTO;
import com.planup.planup.domain.user.dto.TermsDetailResponseDTO;
import com.planup.planup.domain.user.dto.TermsListResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TermsService {

    // 모든 약관 목록 조회 (체크박스 표시용)
    List<TermsListResponseDTO> getTermsList();

    // 특정 약관의 상세 내용 조회 (팝업 표시용)
    TermsDetailResponseDTO getTermsDetail(Long termsId);
}
