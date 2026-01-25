package com.planup.planup.domain.user.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.apiPayload.code.status.SuccessStatus;
import com.planup.planup.domain.user.dto.AuthResponseDTO;
import com.planup.planup.domain.user.service.query.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/terms")
@RequiredArgsConstructor
public class TermsController {

    private final UserQueryService userQueryService;

    @GetMapping
    @Operation(summary = "약관 목록 조회", description = "회원가입 시 체크박스 표시용 약관 목록을 조회합니다.")
    public ApiResponse<AuthResponseDTO.TermsList> getTermsList() {
        return ApiResponse.onSuccess(userQueryService.getTermsList());
    }

    @GetMapping("/{termsId}")
    @Operation(summary = "약관 상세 조회", description = "각 약관의 상세 내용을 조회합니다.")
    public ApiResponse<AuthResponseDTO.TermsDetail> getTermsDetail(
            @PathVariable Long termsId
    ) {
        return ApiResponse.onSuccess(userQueryService.getTermsDetail(termsId));
    }
}
