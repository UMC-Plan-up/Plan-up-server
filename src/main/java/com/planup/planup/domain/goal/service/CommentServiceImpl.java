package com.planup.planup.domain.goal.service;

import com.planup.planup.apiPayload.exception.custom.GoalException;
import com.planup.planup.domain.goal.convertor.CommentConverter;
import com.planup.planup.domain.goal.dto.CommentRequestDto;
import com.planup.planup.domain.goal.dto.CommentResponseDto;
import com.planup.planup.domain.goal.entity.Comment;
import com.planup.planup.domain.goal.entity.Enum.CommentStatus;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goal.repository.CommentRepository;
import com.planup.planup.domain.goal.repository.GoalRepository;
import com.planup.planup.domain.goal.repository.UserGoalRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final GoalRepository goalRepository;
    private final UserGoalRepository userGoalRepository;
    private final UserService userService;

    @Override
    @Transactional
    public CommentResponseDto.CommentDto createComment(Long goalId, Long userId, CommentRequestDto.CommentCreateRequestDto requestDto) {
        validateUserGoalParticipation(goalId, userId);

        User writer = userService.getUserbyUserId(userId);
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new GoalException(ErrorStatus.NOT_FOUND_GOAL));

        Comment parentComment = null;
        if (requestDto.isReply()) {
            parentComment = commentRepository.findById(requestDto.getParentCommentId())
                    .orElseThrow(() -> new GoalException(ErrorStatus.NOT_FOUND_COMMENT));

            if (!parentComment.getGoal().getId().equals(goalId)) {
                throw new GoalException(ErrorStatus.INVALID_PARENT_COMMENT);
            }
        }

        Comment comment = CommentConverter.createComment(
                requestDto.getContent(), writer, goal, parentComment);
        Comment savedComment = commentRepository.save(comment);

        return CommentConverter.toResponseDto(savedComment, userId);
    }

    @Override
    public List<CommentResponseDto.CommentDto> getComments(Long goalId, Long userId) {
        validateUserGoalParticipation(goalId, userId);

        List<Comment> comments = commentRepository.findByGoalIdAndStatusOrderByCreatedAtAsc(goalId, CommentStatus.ACTIVE);

        return comments.stream()
                .map(comment -> CommentConverter.toResponseDto(comment, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentResponseDto.CommentDto updateComment(Long commentId, Long userId, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new GoalException(ErrorStatus.NOT_FOUND_COMMENT));

        if (!comment.getWriter().getId().equals(userId)) {
            throw new GoalException(ErrorStatus.UNAUTHORIZED_COMMENT_ACCESS);
        }

        if (comment.getStatus() != CommentStatus.ACTIVE) {
            throw new GoalException(ErrorStatus.INACTIVE_COMMENT);
        }

        comment.updateContent(content);

        return CommentConverter.toResponseDto(comment, userId);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new GoalException(ErrorStatus.NOT_FOUND_COMMENT));

        if (!comment.getWriter().getId().equals(userId)) {
            throw new GoalException(ErrorStatus.UNAUTHORIZED_COMMENT_ACCESS);
        }

        comment.updateStatus(CommentStatus.INACTIVE);
    }

    private void validateUserGoalParticipation(Long goalId, Long userId) {
        UserGoal userGoal = userGoalRepository.findByGoalIdAndUserId(goalId, userId);
        if (userGoal == null) {
            throw new GoalException(ErrorStatus.UNAUTHORIZED_GOAL_ACCESS);
        }
    }
}
