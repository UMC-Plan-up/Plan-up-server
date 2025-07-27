package com.planup.planup.domain.goal.entity.mapping;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import com.planup.planup.domain.goal.entity.Comment;
import com.planup.planup.domain.goal.entity.Enum.Status;
import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.PhotoVerification;
import com.planup.planup.domain.goal.entity.TimerVerification;
import com.planup.planup.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserGoal extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Status status;
    private boolean isActive;
    private boolean isPublic;
    private String currentAmount;

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
}
