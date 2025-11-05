package com.planup.planup.domain.verification.entity;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class TimerVerification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //타입 통일
    private LocalDateTime endTime;
    private Long spentTimeSeconds;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usergoal_id")
    private UserGoal userGoal;

    public Duration getSpentTime() {
        return Duration.ofSeconds(spentTimeSeconds);
    }

    public void setSpenTime(Duration duration) {
        this.spentTimeSeconds = duration.getSeconds();
    }
}
