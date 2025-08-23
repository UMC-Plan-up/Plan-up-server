package com.planup.planup.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageUploadResponseDTO {
    private String imageUrl;
}
