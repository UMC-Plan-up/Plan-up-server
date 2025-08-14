package com.planup.planup.domain.verification.service;

import com.planup.planup.domain.global.service.ImageUploadService;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goal.repository.UserGoalRepository;
import com.planup.planup.domain.goal.service.UserGoalService;
import com.planup.planup.domain.goal.service.ChallengeService;
import com.planup.planup.domain.verification.entity.PhotoVerification;
import com.planup.planup.domain.verification.entity.TimerVerification;
import com.planup.planup.domain.verification.repository.PhotoVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PhotoVerificationService implements VerificationService {

    private final UserGoalRepository userGoalRepository;
    private final PhotoVerificationRepository photoVerificationRepository;
    private final ImageUploadService imageUploadService;
    private final ChallengeService challengeService;

    public List<PhotoVerification> getAllVerificationByUserGoal(UserGoal userGoal) {
        return photoVerificationRepository.findAllByUserGoal(userGoal);
    }
    private final UserGoalService userGoalService;

    @Transactional
    public void uploadPhotoVerification(
            Long userId,
            Long goalId,
            MultipartFile photoFile) {

        UserGoal userGoal = userGoalService.getByGoalIdAndUserId(goalId, userId);

        String photoUrl = imageUploadService.uploadImage(photoFile, "verifications/photos");

        PhotoVerification photoVerification = PhotoVerification.builder()
                .photoImg(photoUrl)
                .userGoal(userGoal)
                .build();

        PhotoVerification savedVerification = photoVerificationRepository.save(photoVerification);

        userGoal.increaseVerificationCount();
        userGoalRepository.save(userGoal);

        if (userGoal.getGoal().isChallenge()) {
            challengeService.checkChallengeFin(userGoal);
        }
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
            userGoal.decreaseVerificationCount();
            userGoalRepository.save(userGoal);
        }

        photoVerificationRepository.delete(verification);
    }
}