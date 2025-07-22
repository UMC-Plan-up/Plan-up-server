package com.planup.planup.domain.goal.entity;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import com.planup.planup.domain.goal.entity.Enum.GoalPeriod;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import jakarta.persistence.*;

import lombok.Getter;

import java.sql.Time;
import java.time.Duration;

@Entity
@Getter
public class TimerVerification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //타입 통일
    private int goalTime;
    private Duration spentTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usergoal_id")
    private UserGoal userGoal;
}
