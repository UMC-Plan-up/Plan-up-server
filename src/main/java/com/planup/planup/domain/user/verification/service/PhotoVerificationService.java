package com.planup.planup.domain.user.verification.service;

import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.verification.entity.PhotoVerification;
import com.planup.planup.domain.user.verification.repository.PhotoVerificationRepository;
import com.planup.planup.domain.goal.repository.UserGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhotoVerificationService implements VerificationService{
    private final UserGoalRepository userGoalRepository;
    private final PhotoVerificationRepository photoVerificationRepository;

    @Transactional(readOnly = true)
    public List<PhotoVerification> getPhotoVerificatonListByUserAndDateBetween(User user, LocalDateTime start, LocalDateTime end) {
        return  photoVerificationRepository.findAllByUserGoalAndCreatedAtBetween(user, start, end);
    }
}
