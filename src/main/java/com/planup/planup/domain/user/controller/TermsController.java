package com.planup.planup.domain.user.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.user.dto.AuthResponseDTO;
import com.planup.planup.domain.user.service.TermsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/terms")
@RequiredArgsConstructor
public class TermsController {

    private final TermsService termsService;

    @GetMapping
    @Operation(summary = "약관 목록 조회", description = "회원가입 시 체크박스 표시용 약관 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<AuthResponseDTO.TermsList>>> getTermsList() {
        List<AuthResponseDTO.TermsList> termsList = termsService.getTermsList();
        return ResponseEntity.ok(ApiResponse.onSuccess(termsList));
    }

    @GetMapping("/{termsId}")
    @Operation(summary = "약관 상세 조회", description = "팝업에 표시할 약관 상세 내용을 조회합니다.")
    public ResponseEntity<ApiResponse<AuthResponseDTO.TermsDetail>> getTermsDetail(
            @PathVariable Long termsId) {

        AuthResponseDTO.TermsDetail termsDetail = termsService.getTermsDetail(termsId);
        return ResponseEntity.ok(ApiResponse.onSuccess(termsDetail));
    }
}
