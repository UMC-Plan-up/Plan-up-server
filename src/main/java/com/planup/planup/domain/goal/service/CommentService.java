package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.goal.dto.CommentRequestDto;
import com.planup.planup.domain.goal.dto.CommentResponseDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CommentService {
    CommentResponseDto.CommentDto createCommentByGoal(Long goalId, Long userId, CommentRequestDto.CommentCreateRequestDto requestDto);

    @Transactional
    CommentResponseDto.CommentDto createCommentByGoalReport(Long reportId, Long userId, CommentRequestDto.CommentCreateRequestDto requestDto);

    List<CommentResponseDto.CommentDto> getComments(Long goalId, Long userId);
    CommentResponseDto.CommentDto updateComment(Long commentId, Long userId, String content);
    void deleteComment(Long commentId, Long userId);
}
