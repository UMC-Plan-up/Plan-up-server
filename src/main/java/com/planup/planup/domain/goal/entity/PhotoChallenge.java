package com.planup.planup.domain.goal.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@DiscriminatorValue("CHALLENGE_PHOTO")
public class PhotoChallenge extends Challenge {

    private Date endDate;
    private int timePerPeriod;  // 기준 기간 (예: 7일)
    private int frequency;      // 빈도
}
