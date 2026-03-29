package com.planup.planup.domain.complaint.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.complaint.dto.ComplaintRequestDTO;
import com.planup.planup.domain.complaint.service.CommentComplaintService;
import com.planup.planup.validation.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentComplaintController {

    private final CommentComplaintService commentComplaintService;

    @Operation(
            summary = "댓글 신고",
            description = """
                    댓글을 신고합니다.
                    - 즉시 신고자 기준 해당 댓글 가림 처리
                    - 3회 누적: 댓글 작성자에게 유저 신고 1회 적용

                    신고 사유 (reason)
                    - ABUSE_OR_HATE_SPEECH: 욕설/비방/혐오 표현 사용
                    - SEXUAL_CONTENT: 음란물/선정적 내용
                    - SPAM_OR_ADVERTISING: 스팸/광고
                    - INAPPROPRIATE_CONTENT: 불쾌하거나 부적절한 내용
                    - FRAUD_OR_IMPERSONATION: 거짓 정보 및 사칭
                    - OTHER: 기타
                    """
    )
    @PostMapping("/{commentId}/complaints")
    public ApiResponse<Void> reportComment(
            @CurrentUser Long userId,
            @PathVariable Long commentId,
            @RequestBody ComplaintRequestDTO request) {
        commentComplaintService.reportComment(userId, commentId, request);
        return ApiResponse.onSuccess(null);
    }
}
