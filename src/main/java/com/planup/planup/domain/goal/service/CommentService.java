package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.goal.dto.CommentRequestDto;
import com.planup.planup.domain.goal.dto.CommentResponseDto;
import com.planup.planup.domain.goal.entity.Comment;

import java.util.List;

public interface CommentService {
    CommentResponseDto.CommentDto createComment(Long goalId, Long userId, CommentRequestDto.CommentCreateRequestDto requestDto);
    List<CommentResponseDto.CommentDto> getComments(Long goalId, Long userId);
    CommentResponseDto.CommentDto updateComment(Long commentId, Long userId, String content);
    void deleteComment(Long commentId, Long userId);
}
