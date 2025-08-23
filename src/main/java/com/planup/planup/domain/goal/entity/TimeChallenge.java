package com.planup.planup.domain.goal.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@DiscriminatorValue("CHALLENGE_TIME")
public class TimeChallenge extends Challenge {

    private Long targetTime;  // 초 단위로 저장 추천
}
