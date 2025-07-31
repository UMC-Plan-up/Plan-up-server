package com.planup.planup.domain.user.verification.service;

import com.planup.planup.domain.user.verification.repository.PhotoVerificationRepository;
import com.planup.planup.domain.goal.repository.UserGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PhotoVerificationService implements VerificationService{
    private final UserGoalRepository userGoalRepository;
    private final PhotoVerificationRepository photoVerificationRepository;


}
