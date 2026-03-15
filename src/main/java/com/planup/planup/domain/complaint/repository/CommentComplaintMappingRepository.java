package com.planup.planup.domain.complaint.repository;

import com.planup.planup.domain.complaint.entity.CommentComplaintMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentComplaintMappingRepository extends JpaRepository<CommentComplaintMapping, Long> {

    boolean existsByReporterIdAndCommentId(Long reporterId, Long commentId);

    // 신고자가 신고한 commentId 목록 (조회 필터링용)
    @Query("SELECT c.comment.id FROM CommentComplaintMapping c WHERE c.reporter.id = :reporterId")
    List<Long> findReportedCommentIdsByReporterId(@Param("reporterId") Long reporterId);
}
