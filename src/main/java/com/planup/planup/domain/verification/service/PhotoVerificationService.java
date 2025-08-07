package com.planup.planup.domain.verification.service;

import com.planup.planup.domain.global.service.ImageUploadService;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goal.repository.UserGoalRepository;
import com.planup.planup.domain.verification.entity.PhotoVerification;
import com.planup.planup.domain.verification.repository.PhotoVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhotoVerificationService implements VerificationService {

    private final UserGoalRepository userGoalRepository;
    private final PhotoVerificationRepository photoVerificationRepository;
    private final ImageUploadService imageUploadService;

    @Transactional
    public void uploadPhotoVerification(
            Long userId,
            Long goalId,
            MultipartFile photoFile) {

        UserGoal userGoal = userGoalRepository.findByGoalIdAndUserId(goalId, userId);

        String photoUrl = imageUploadService.uploadImage(photoFile, "verifications/photos");

        PhotoVerification photoVerification = PhotoVerification.builder()
                .photoImg(photoUrl)
                .userGoal(userGoal)
                .build();

        PhotoVerification savedVerification = photoVerificationRepository.save(photoVerification);

        userGoal.setVerificationCount(userGoal.getVerificationCount() + 1);
        userGoalRepository.save(userGoal);
    }

    @Transactional
    public void deletePhotoVerification(Long userId, Long verificationId) {
        PhotoVerification verification = photoVerificationRepository.findById(verificationId)
                .orElseThrow(() -> new RuntimeException("해당 사진 인증을 찾을 수 없습니다."));

        if (!verification.getUserGoal().getUser().getId().equals(userId)) {
            throw new RuntimeException("해당 사진을 삭제할 권한이 없습니다.");
        }

        UserGoal userGoal = verification.getUserGoal();
        if (userGoal.getVerificationCount() > 0) {
            userGoal.setVerificationCount(userGoal.getVerificationCount() - 1);
            userGoalRepository.save(userGoal);
        }

        photoVerificationRepository.delete(verification);
    }

    @Transactional(readOnly = true)
    public List<PhotoVerification> getPhotoVerificationListByUserAndDateBetween(UserGoal userGoal, LocalDateTime start, LocalDateTime end) {
        return photoVerificationRepository.findAllByUserGoalAndCreatedAtBetweenOrderByCreatedAt(userGoal,start,end);
    }
}