package com.planup.planup.domain.bedge.converter;

import com.planup.planup.domain.bedge.dto.BadgeResponseDTO;
import com.planup.planup.domain.bedge.entity.Badge;
import com.planup.planup.domain.user.entity.UserBadge;

import java.util.List;
import java.util.stream.Collectors;

public class BadgeConverter {


    public static BadgeResponseDTO.SimpleBadgeDTO toSimpleBadgeDTO(Badge badge) {
        return BadgeResponseDTO.SimpleBadgeDTO.builder()
                .id(badge.getId())
                .badgeName(badge.getBadgeName())
                .badgeType(badge.getBadgeType())
                .build();
    }

}
