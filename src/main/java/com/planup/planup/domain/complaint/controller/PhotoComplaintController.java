package com.planup.planup.domain.complaint.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.complaint.dto.ComplaintRequestDTO;
import com.planup.planup.domain.complaint.service.PhotoComplaintService;
import com.planup.planup.validation.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/goal-photos")
public class PhotoComplaintController {

    private final PhotoComplaintService photoComplaintService;

    @Operation(
            summary = "사진 신고",
            description = """
                    목표 인증 사진을 신고합니다.
                    - 즉시 신고자 기준 해당 사진 가림 처리
                    - 3회 누적: 사진 업로더에게 유저 신고 1회 적용
                    - 관리자 검토 후 삭제 확정 시 달성률 계산에서 제외

                    신고 사유 (reason)
                    - ABUSE_OR_HATE_SPEECH: 욕설/비방/혐오 표현 사용
                    - SEXUAL_CONTENT: 음란물/선정적 내용
                    - SPAM_OR_ADVERTISING: 스팸/광고
                    - INAPPROPRIATE_CONTENT: 불쾌하거나 부적절한 내용
                    - FRAUD_OR_IMPERSONATION: 거짓 정보 및 사칭
                    - OTHER: 기타
                    """
    )
    @PostMapping("/{photoId}/complaints")
    public ApiResponse<Void> reportPhoto(
            @CurrentUser Long userId,
            @PathVariable Long photoId,
            @RequestBody ComplaintRequestDTO request) {
        photoComplaintService.reportPhoto(userId, photoId, request);
        return ApiResponse.onSuccess(null);
    }
}
