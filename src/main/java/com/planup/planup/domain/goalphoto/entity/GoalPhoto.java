package com.planup.planup.domain.goalphoto.entity;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "goal_photo",
        indexes = {
                @Index(name = "idx_goal_photo_usergoal_date", columnList = "user_goal_id, date")
        }
)
public class GoalPhoto extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String photoUrl;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_goal_id", nullable = false)
    private UserGoal userGoal;
}
