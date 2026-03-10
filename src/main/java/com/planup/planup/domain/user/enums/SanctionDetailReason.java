package com.planup.planup.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SanctionDetailReason {
    ABUSE_OR_HATE_SPEECH("욕설/비방/혐오 표현 사용"),
    SEXUAL_CONTENT("음란물/선정적 내용"),
    SPAM_OR_ADVERTISING("스팸/광고"),
    INAPPROPRIATE_CONTENT("불쾌하거나 부적절한 내용"),
    FRAUD_OR_IMPERSONATION("거짓 정보 및 사칭"),
    OTHER("기타");

    private final String description;
}
