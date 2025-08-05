package com.planup.planup.domain.user.verification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

public class PhotoVerificationResponseDto {

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class uploadPhotoResponseDto {
        private Long verificationId;
        private Long goalId;
        private String photoImg;
    }
}
