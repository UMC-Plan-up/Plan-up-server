package com.planup.planup.domain.verification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PhotoVerificationResponseDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class uploadPhotoResponseDto {
        private Long verificationId;
        private Long goalId;
        private String photoImg;
    }
}
