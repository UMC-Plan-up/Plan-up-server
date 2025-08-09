package com.planup.planup.domain.report.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ThreeWeekAchievementRate {

    private int thisWeek;
    private int oneWeekBefore;
    private int twoWeekBefore;
}
