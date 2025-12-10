package com.planup.planup.domain.bedge.entity;

import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.user.entity.User;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "specific_goal_days",
        indexes = {
                @Index(name = "idx_user_goal", columnList = "user_id, goal_id, updatedAt")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_goal", columnNames = {"user_id", "goal_id"})
        }
)
@Getter
@Builder @AllArgsConstructor @NoArgsConstructor
public class SpecificGoalDays {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NonNull
    private LocalDate lastUpdate;               //가장 최근에 업데이트한 날짜.

    @Builder.Default
    private int consecutiveSuccessDays = 1;

    public void updateNewRecord() {
        this.consecutiveSuccessDays++;
        this.lastUpdate = LocalDate.now();
    }

    public boolean isUpdatableThanUpdate() {
        if (this.getLastUpdate().equals(LocalDate.now().minusDays(1))){
            updateNewRecord();
            return true;
        } else if (this.getLastUpdate().equals(LocalDate.now())) {
            return true;
        } else {
            return false;
        }
    }

    public SpecificGoalDays(Goal newGoal, User newUser) {
        this.goal = newGoal;
        this.user = newUser;
        this.lastUpdate = LocalDate.now();
        this.consecutiveSuccessDays = 1;
    }
}
