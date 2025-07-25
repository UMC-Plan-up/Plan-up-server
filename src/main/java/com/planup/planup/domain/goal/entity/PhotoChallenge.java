package com.planup.planup.domain.goal.entity;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Calendar;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhotoChallenge extends Challenge {

    private LocalDateTime endDate;
    private int timePerPeriod;  // 기준 기간 (예: 7일)
    private int frequency;      // 빈도
}
