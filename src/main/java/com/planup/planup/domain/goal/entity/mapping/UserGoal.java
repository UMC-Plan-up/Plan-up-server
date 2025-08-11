package com.planup.planup.domain.goal.entity.mapping;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.GeneralException;
import com.planup.planup.apiPayload.exception.custom.ChallengeException;
import com.planup.planup.domain.global.entity.BaseTimeEntity;
import com.planup.planup.domain.goal.entity.Enum.Status;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.verification.entity.PhotoVerification;
import com.planup.planup.domain.verification.entity.TimerVerification;
import com.planup.planup.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@SuperBuilder
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserGoal extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Status status;
    private boolean isActive;
    private boolean isPublic;
    private String currentAmount;
    private int verificationCount;
    //이 필드 골 로 이동
    private int goalTime;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private Goal goal;

    @OneToMany(mappedBy = "userGoal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimerVerification> timerVerifications = new ArrayList<>();

    @OneToMany(mappedBy = "userGoal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhotoVerification> photoVerifications = new ArrayList<>();

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

    public void setInActive() {
        this.isActive = false;
    }
}
