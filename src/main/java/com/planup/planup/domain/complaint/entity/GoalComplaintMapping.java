package com.planup.planup.domain.complaint.entity;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.enums.SanctionDetailReason;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "goal_complaint_mapping",
        indexes = {
                @Index(name = "idx_goal_complaint_reporter_goal", columnList = "reporter_id, goal_id"),
                @Index(name = "idx_goal_complaint_goal", columnList = "goal_id")
        }
)
public class GoalComplaintMapping extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SanctionDetailReason reason;
}
