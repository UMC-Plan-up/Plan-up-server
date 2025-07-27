package com.planup.planup.domain.goal.entity;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import com.planup.planup.domain.goal.entity.Enum.GoalCategory;
import com.planup.planup.domain.goal.entity.Enum.GoalType;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "goal_type")
public class Goal extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String goalName;
    private String goalAmount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private GoalCategory goalCategory;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private GoalType goalType;

    private String oneDose;
    private LocalDateTime endDate;

    private Boolean isChallenge;
    private String currentAmount;
    private int limitFriendCount;

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL)
    private List<Comment> commentList = new ArrayList<>();

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserGoal> userGoals = new ArrayList<>();

    public void addUserGoal(UserGoal userGoal) {
        userGoals.add(userGoal);
        userGoal.setGoal(this);
    }

}
