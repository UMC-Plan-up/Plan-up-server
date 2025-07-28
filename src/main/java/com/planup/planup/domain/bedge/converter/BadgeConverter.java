package com.planup.planup.domain.bedge.converter;

import com.planup.planup.domain.bedge.dto.BadgeResponseDTO;
import com.planup.planup.domain.bedge.entity.BadgeType;
import com.planup.planup.domain.user.entity.UserBadge;

import java.util.List;
import java.util.stream.Collectors;

public class BadgeConverter {

    public static BadgeResponseDTO.SimpleBadgeDTO toSimpleBadgeDTO(BadgeType badge) {
        return BadgeResponseDTO.SimpleBadgeDTO.builder()
                .badgeName(badge.getDisplayName())
                .badgeType(badge)
                .build();
    }

}
