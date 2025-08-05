package com.planup.planup.domain.user.verification.service;

import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VerificationServiceFactory {
    private final TimerVerificationService timerVerificationService;
    private final PhotoVerificationService photoVerificationService;

    public VerificationService getService(VerificationType type) {
        return switch (type) {
            case TIMER -> timerVerificationService;
            case PHOTO -> photoVerificationService;
            default -> throw new IllegalArgumentException("지원하지 않는 인증 타입: " + type);
        };
    }
}
