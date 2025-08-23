package com.planup.planup.domain.goal.dto;

import lombok.*;

public class CommentResponseDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentDto {

        private Long id;
        private String content;
        private Long writerId;
        private String writerNickname;
        private String writerProfileImg;
        private Long parentCommentId;
        private String parentCommentContent;
        private String parentCommentWriter;
        private boolean isMyComment;

        public boolean isReply() {
            return parentCommentId != null;
        }
    }
}
