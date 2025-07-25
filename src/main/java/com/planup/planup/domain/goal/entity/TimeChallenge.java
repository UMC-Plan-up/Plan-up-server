package com.planup.planup.domain.goal.entity;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeChallenge extends Challenge {

    private Long targetTime;  // 초 단위로 저장 추천
}
