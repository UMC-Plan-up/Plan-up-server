package com.planup.planup.domain.user.service;

import com.planup.planup.domain.user.dto.AuthResponseDTO;

import java.util.List;

public interface TermsService {

    // 모든 약관 목록 조회 (체크박스 표시용)
    List<AuthResponseDTO.TermsList> getTermsList();

    // 특정 약관의 상세 내용 조회 (팝업 표시용)
    AuthResponseDTO.TermsDetail getTermsDetail(Long termsId);
}
