package com.planup.planup.domain.goal.entity.mapping;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.GeneralException;
import com.planup.planup.domain.global.entity.BaseTimeEntity;
import com.planup.planup.domain.goal.entity.Enum.Status;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.verification.entity.PhotoVerification;
import com.planup.planup.domain.verification.entity.TimerVerification;
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

    // ADMIN, MEMBER 등의 상태
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status status;

    // boolean 타입은 길이 설정 불필요
    private boolean isActive;
    private boolean isPublic;

    // 현재 달성량 - "15분", "3권", "2500ml" 등의 형태
    @Column(length = 100)
    private String currentAmount;

    // 인증 횟수
    private int verificationCount;

    // 목표 시간(분 단위)
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
        if (this.user.getId().equals(user.getId())) {
            isActive = set;
        } else {
            throw new GeneralException(ErrorStatus._NOT_ALLOWED);
        }
    }

    public void setUser(User user) {
        this.user = user;
        if (user.getUserGoals() != null && !user.getUserGoals().contains(this)) {
            user.getUserGoals().add(this);
        }
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
        if (goal.getUserGoals() != null && !goal.getUserGoals().contains(this)) {
            goal.getUserGoals().add(this);
        }
    }
}