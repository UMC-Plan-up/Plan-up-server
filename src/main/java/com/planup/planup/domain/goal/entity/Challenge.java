package com.planup.planup.domain.goal.entity;

import com.planup.planup.domain.goal.entity.Enum.ChallengeStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
@SuperBuilder
@AllArgsConstructor
public class Challenge extends Goal {


    @Enumerated(EnumType.STRING)
    private ChallengeStatus status;  // 거절/수락 상태

    private String penalty;
    private String rePenalty;
}
