package com.planup.planup.domain.bedge.service.badge;

import com.planup.planup.domain.bedge.converter.BadgeConverter;
import com.planup.planup.domain.bedge.dto.BadgeResponseDTO;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.entity.UserBadge;
import com.planup.planup.domain.user.repository.UserBadgeRepository;
import com.planup.planup.domain.user.repository.UserRepository;
import com.planup.planup.domain.user.service.query.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BadgeQueryServiceImpl implements BadgeQueryService {

    private final UserBadgeRepository userBadgeRepository;
    private final UserQueryService userQueryService;

    public List<BadgeResponseDTO.SimpleBadgeDTO> getUserBadgeList(Long userId) {
        User user = userQueryService.getUserByUserId(userId);
        List<UserBadge> userBadges = userBadgeRepository.findAllByUser(user);
        return userBadges.stream().map(ub -> BadgeConverter.toSimpleBadgeDTO(ub.getBadgeType())).collect(Collectors.toList());
    }
}
