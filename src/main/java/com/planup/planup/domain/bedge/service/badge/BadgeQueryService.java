package com.planup.planup.domain.bedge.service.badge;

import com.planup.planup.domain.bedge.dto.BadgeResponseDTO;

import java.util.List;

public interface BadgeQueryService {
    List<BadgeResponseDTO.SimpleBadgeDTO> getUserBadgeList(Long userId);
}
