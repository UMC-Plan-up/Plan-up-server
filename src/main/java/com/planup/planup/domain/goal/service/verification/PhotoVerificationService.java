package com.planup.planup.domain.goal.service.verification;

import com.planup.planup.domain.goal.repository.PhotoVerificationRepository;
import com.planup.planup.domain.goal.repository.UserGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PhotoVerificationService implements VerificationService{
    private final UserGoalRepository userGoalRepository;
    private final PhotoVerificationRepository photoVerificationRepository;


}
