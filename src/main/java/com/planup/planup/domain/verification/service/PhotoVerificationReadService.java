package com.planup.planup.domain.verification.service;

import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.verification.entity.PhotoVerification;
import com.planup.planup.domain.verification.repository.PhotoVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
@Service
@RequiredArgsConstructor
public class PhotoVerificationReadService {

    private final PhotoVerificationRepository photoVerificationRepository;

    @Transactional(readOnly = true)
    public List<PhotoVerification> getPhotoVerificationListByUserAndDateBetween(UserGoal userGoal, LocalDateTime start, LocalDateTime end) {
        return photoVerificationRepository.findAllByUserGoalAndCreatedAtBetweenOrderByCreatedAt(userGoal,start,end);
    }

    @Transactional(readOnly = true)
    public Map<LocalDate, Integer> calculateVerification(UserGoal userGoal, LocalDateTime startDate, LocalDateTime endDate) {
        List<PhotoVerification> verifications = getPhotoVerificationListByUserAndDateBetween(userGoal, startDate, endDate);

        Map<LocalDate, Integer> dailyCount = new HashMap<>();
        // 날짜별 인증 수 카운팅
        for (PhotoVerification photoVerification : verifications) {
            LocalDate date = photoVerification.getCreatedAt().toLocalDate();

            int photoCount = photoVerification.getPhotoImgs() != null ? photoVerification.getPhotoImgs().size() : 0;

            //기존에 데이터가 있으면 불러와서 더한다.
            dailyCount.put(date, dailyCount.getOrDefault(date, 0) + photoCount);
        }
        return dailyCount;
    }
}
