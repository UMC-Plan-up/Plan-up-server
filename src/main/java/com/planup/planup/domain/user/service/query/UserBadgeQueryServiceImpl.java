package com.planup.planup.domain.user.service.query;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.bedge.entity.BadgeType;
import com.planup.planup.domain.bedge.entity.UserStat;
import com.planup.planup.domain.user.converter.TermsConverter;
import com.planup.planup.domain.user.dto.AuthResponseDTO;
import com.planup.planup.domain.user.entity.Terms;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.entity.UserBadge;
import com.planup.planup.domain.user.repository.TermsRepository;
import com.planup.planup.domain.user.repository.UserBadgeRepository;
import com.planup.planup.domain.user.repository.UserStatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserBadgeQueryServiceImpl implements UserBadgeQueryService {

    private final UserBadgeRepository userBadgeRepository;
    private final UserStatRepository userStatRepository;
    private final UserQueryService userQueryService;

    // ========== 뱃지 조회 ==========

    @Override
    public List<UserBadge> getUserBadgeInPeriod(User user, LocalDateTime from, LocalDateTime to) {
        return userBadgeRepository.findByUserAndCreatedAtBetween(user, from, to);
    }

    @Override
    public List<BadgeType> getBadgeInPeriod(User user, LocalDateTime from, LocalDateTime to) {
        return userBadgeRepository.findByUserAndCreatedAtBetween(user, from, to)
                .stream()
                .map(UserBadge::getBadgeType)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserBadge> getTop5Recent(User user) {
        return userBadgeRepository.findTop5ByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public List<BadgeType> getBadgeByUser(User user) {
        List<UserBadge> userbadges = userBadgeRepository.findByUserOrderByCreatedAtDesc(user);
        return userbadges.stream()
                .map(UserBadge::getBadgeType)
                .collect(Collectors.toList());
    }

    // ========== 통계 조회 ==========

    @Override
    public UserStat getUserStatByUserId(Long userId) {
        User user = userQueryService.getUserByUserId(userId);
        return userStatRepository.findByUser(user);
    }
}