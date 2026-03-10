package com.planup.planup.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SanctionReason {
    USER_REPORT("유저 신고");

    private final String description;
}
