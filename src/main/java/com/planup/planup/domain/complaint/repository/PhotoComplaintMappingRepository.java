package com.planup.planup.domain.complaint.repository;

import com.planup.planup.domain.complaint.entity.PhotoComplaintMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhotoComplaintMappingRepository extends JpaRepository<PhotoComplaintMapping, Long> {

    boolean existsByReporterIdAndPhotoId(Long reporterId, Long photoId);

    // 신고자가 신고한 photoId 목록 (조회 필터링용)
    @Query("SELECT p.photo.id FROM PhotoComplaintMapping p WHERE p.reporter.id = :reporterId")
    List<Long> findReportedPhotoIdsByReporterId(@Param("reporterId") Long reporterId);
}
