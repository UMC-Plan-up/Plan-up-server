package com.planup.planup.domain.goal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
public class CommentRequestDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "댓글 생성 요청 DTO")
    public static class CommentCreateRequestDto {

        @NotBlank(message = "댓글 내용을 입력해주세요.")
        @Size(max = 500, message = "댓글은 500자 이내로 입력해주세요.")
        @Schema(description = "댓글 내용", example = "좋은 목표네요! 저도 따라해보겠습니다.")
        private String content;

        @Schema(description = "대댓글 대상 사용자 ID (대댓글인 경우에만 필요)")
        private Long parentCommentId;

        @Schema(description = "대댓글 여부", example = "false")
        public boolean isReply() {
            return parentCommentId != null;
        }
    }
}
