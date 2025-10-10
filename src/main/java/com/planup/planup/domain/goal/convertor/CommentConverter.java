package com.planup.planup.domain.goal.convertor;

import com.planup.planup.domain.goal.dto.CommentResponseDto;
import com.planup.planup.domain.goal.entity.Comment;
import com.planup.planup.domain.goal.entity.Enum.CommentStatus;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.report.entity.GoalReport;
import com.planup.planup.domain.user.entity.User;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    public static Comment createCommentForReport(String content, User writer, GoalReport goalReport, Comment parentComment) {
        return Comment.builder()
                .content(content)
                .writer(writer)
                .goalReport(goalReport)
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

    public static List<CommentResponseDto.CommentDto> toResponseDtoList(List<Comment> comments, Long currentUserId) {
        return Optional.ofNullable(comments)
                .orElse(Collections.emptyList())
                .stream()
                .map(c -> CommentConverter.toResponseDto(c, currentUserId))
                .toList();
    }
}
