package com.planup.planup.domain.complaint.service;

import com.planup.planup.domain.complaint.dto.ComplaintRequestDTO;
import com.planup.planup.domain.complaint.entity.PhotoComplaintMapping;
import com.planup.planup.domain.complaint.repository.PhotoComplaintMappingRepository;
import com.planup.planup.domain.friend.service.reportUserService.UserReportMappingService;
import com.planup.planup.domain.goalphoto.entity.GoalPhoto;
import com.planup.planup.domain.goalphoto.repository.GoalPhotoRepository;
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
public class PhotoComplaintServiceImpl implements PhotoComplaintService {

    private final PhotoComplaintMappingRepository photoComplaintMappingRepository;
    private final GoalPhotoRepository goalPhotoRepository;
    private final UserQueryService userQueryService;
    private final UserReportMappingService userReportMappingService;

    @Override
    @Transactional
    public void reportPhoto(Long reporterId, Long photoId, ComplaintRequestDTO request) {
        if (photoComplaintMappingRepository.existsByReporterIdAndPhotoId(reporterId, photoId)) {
            throw new IllegalStateException("이미 신고한 사진입니다.");
        }

        User reporter = userQueryService.getUserByUserId(reporterId);
        GoalPhoto photo = goalPhotoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사진입니다."));

        PhotoComplaintMapping complaint = PhotoComplaintMapping.builder()
                .reporter(reporter)
                .photo(photo)
                .reason(request.getReason())
                .build();

        photoComplaintMappingRepository.save(complaint);

        photo.incrementComplaintCount();

        // 사진 3회 누적 = 사진 업로더에게 유저 신고 1회 적용
        if (photo.getComplaintCount() % 3 == 0) {
            Long uploaderId = photo.getUserGoal().getUser().getId();
            userReportMappingService.createSystemReportUser(reporterId, uploaderId, request.getReason());
            log.info("사진 누적 신고로 유저 신고 적용: photoId={}, uploaderId={}, complaintCount={}",
                    photoId, uploaderId, photo.getComplaintCount());
        }
    }

    @Override
    public List<Long> getReportedPhotoIds(Long reporterId) {
        return photoComplaintMappingRepository.findReportedPhotoIdsByReporterId(reporterId);
    }
}
