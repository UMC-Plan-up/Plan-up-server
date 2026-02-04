package com.planup.planup.domain.bedge.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.bedge.dto.BadgeResponseDTO;
import com.planup.planup.domain.bedge.service.badge.BadgeQueryService;
import com.planup.planup.validation.annotation.CurrentUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/badges")
public class BadgeController {

    private final BadgeQueryService badgeQueryService;

    @GetMapping("/list")
    public ApiResponse<List<BadgeResponseDTO.SimpleBadgeDTO>> getAllSimpleBadgeByUser(@CurrentUser Long userId) {
        List<BadgeResponseDTO.SimpleBadgeDTO> userBadgeList = badgeQueryService.getUserBadgeList(userId);
        return ApiResponse.onSuccess(userBadgeList);
    }
}
