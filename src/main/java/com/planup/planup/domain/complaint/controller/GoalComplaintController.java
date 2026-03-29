package com.planup.planup.domain.complaint.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.complaint.dto.ComplaintRequestDTO;
import com.planup.planup.domain.complaint.service.GoalComplaintService;
import com.planup.planup.validation.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/goals")
public class GoalComplaintController {

    private final GoalComplaintService goalComplaintService;

    @Operation(
            summary = "커뮤니티 신고",
            description = """
                    커뮤니티(목표)를 신고합니다.
                    - 1회 신고: 신고자에게 즉시 해당 커뮤니티 조회/합류 제한
                    - 3회 누적: 커뮤니티 14일 비활성화 (신규 합류 불가, 기존 참여자 기록 불가)
                    - 5회 누적: 커뮤니티 영구 삭제

                    신고 사유 (reason)
                    - ABUSE_OR_HATE_SPEECH: 욕설/비방/혐오 표현 사용
                    - SEXUAL_CONTENT: 음란물/선정적 내용
                    - SPAM_OR_ADVERTISING: 스팸/광고
                    - INAPPROPRIATE_CONTENT: 불쾌하거나 부적절한 내용
                    - FRAUD_OR_IMPERSONATION: 거짓 정보 및 사칭
                    - OTHER: 기타
                    """
    )
    @PostMapping("/{goalId}/complaints")
    public ApiResponse<Void> reportGoal(
            @CurrentUser Long userId,
            @PathVariable Long goalId,
            @RequestBody ComplaintRequestDTO request) {
        goalComplaintService.reportGoal(userId, goalId, request);
        return ApiResponse.onSuccess(null);
    }
}
