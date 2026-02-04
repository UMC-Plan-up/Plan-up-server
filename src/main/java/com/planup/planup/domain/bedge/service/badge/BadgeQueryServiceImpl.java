package com.planup.planup.domain.bedge.service.badge;

import com.planup.planup.domain.user.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BadgeQueryServiceImpl implements BadgeQueryService {

    private final UserBadgeRepository userBadgeRepository;


}
