package com.planup.planup.domain.complaint.service;

import com.planup.planup.domain.complaint.dto.ComplaintRequestDTO;
import com.planup.planup.domain.complaint.entity.CommentComplaintMapping;
import com.planup.planup.domain.complaint.repository.CommentComplaintMappingRepository;
import com.planup.planup.domain.friend.service.reportUserService.UserReportMappingService;
import com.planup.planup.domain.goal.entity.Comment;
import com.planup.planup.domain.goal.repository.CommentRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.query.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommentComplaintServiceImpl implements CommentComplaintService {

    private final CommentComplaintMappingRepository commentComplaintMappingRepository;
    private final CommentRepository commentRepository;
    private final UserQueryService userQueryService;
    private final UserReportMappingService userReportMappingService;

    @Override
    @Transactional
    public void reportComment(Long reporterId, Long commentId, ComplaintRequestDTO request) {
        if (commentComplaintMappingRepository.existsByReporterIdAndCommentId(reporterId, commentId)) {
            throw new IllegalStateException("이미 신고한 댓글입니다.");
        }

        User reporter = userQueryService.getUserByUserId(reporterId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        CommentComplaintMapping complaint = CommentComplaintMapping.builder()
                .reporter(reporter)
                .comment(comment)
                .reason(request.getReason())
                .build();

        commentComplaintMappingRepository.save(complaint);

        comment.incrementComplaintCount();

        // 댓글 3회 누적 = 댓글 작성자에게 유저 신고 1회 적용
        if (comment.getComplaintCount() % 3 == 0) {
            Long writerId = comment.getWriter().getId();
            userReportMappingService.createSystemReportUser(reporterId, writerId, request.getReason());
            log.info("댓글 누적 신고로 유저 신고 적용: commentId={}, writerId={}, complaintCount={}",
                    commentId, writerId, comment.getComplaintCount());
        }
    }

    @Override
    public List<Long> getReportedCommentIds(Long reporterId) {
        return commentComplaintMappingRepository.findReportedCommentIdsByReporterId(reporterId);
    }
}
