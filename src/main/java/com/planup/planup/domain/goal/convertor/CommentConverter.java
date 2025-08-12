package com.planup.planup.domain.goal.convertor;

import com.planup.planup.domain.goal.dto.CommentResponseDto;
import com.planup.planup.domain.goal.entity.Comment;
import com.planup.planup.domain.goal.entity.Enum.CommentStatus;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.user.entity.User;

public class CommentConverter {

    public static Comment createComment(String content, User writer, Goal goal, Comment parentComment) {
        return Comment.builder()
                .content(content)
                .writer(writer)
                .goal(goal)
                .parentComment(parentComment)
                .status(CommentStatus.ACTIVE)
                .build();
    }

    public static CommentResponseDto.CommentDto toResponseDto(Comment comment, Long currentUserId) {
        return CommentResponseDto.CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .writerId(comment.getWriter().getId())
                .writerNickname(comment.getWriter().getNickname())
                .writerProfileImg(comment.getWriter().getProfileImg())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .parentCommentContent(comment.getParentComment() != null ? comment.getParentComment().getContent() : null)
                .parentCommentWriter(comment.getParentComment() != null ? comment.getParentComment().getWriter().getNickname() : null)
                .isMyComment(comment.getWriter().getId().equals(currentUserId))
                .build();
    }
}
