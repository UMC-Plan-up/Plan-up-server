package com.planup.planup.domain.report.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ThreeWeekAhcievementRate {

    private int thisWeek;
    private int oneWeekBefore;
    private int twoWeekBefore;
}
