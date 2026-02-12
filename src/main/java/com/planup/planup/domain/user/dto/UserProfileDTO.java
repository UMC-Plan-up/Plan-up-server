package com.planup.planup.domain.user.dto;


public record UserProfileDTO(
        Long userId,
        String name,
        String profileImg
) {
}
