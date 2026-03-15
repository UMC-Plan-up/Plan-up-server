package com.planup.planup.domain.complaint.service;

import com.planup.planup.domain.complaint.dto.ComplaintRequestDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PhotoComplaintService {

    @Transactional
    void reportPhoto(Long reporterId, Long photoId, ComplaintRequestDTO request);

    // 신고자가 신고한 photoId 목록 반환 (조회 필터링용)
    List<Long> getReportedPhotoIds(Long reporterId);
}
