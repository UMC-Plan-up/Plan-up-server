package com.planup.planup.domain.goal.entity.mapping;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.GeneralException;
import com.planup.planup.domain.global.entity.BaseTimeEntity;
import com.planup.planup.domain.goal.entity.Enum.Status;
import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserGoal extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Status status;
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private VerificationType verificationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private Goal goal;

    public void setActive(boolean set, User user) {
        if (user.getId().equals(user.getId())) {
            isActive = set;
        } else {
            throw new GeneralException(ErrorStatus._NOT_ALLOWED);
        }
    }

    public void setUser(User user) {
        this.user = user;
        if (!user.getUserGoals().contains(this)) {
            user.getUserGoals().add(this);
        }
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
        if (!goal.getUserGoals().contains(this)) {
            goal.getUserGoals().add(this);
        }
    }
}
