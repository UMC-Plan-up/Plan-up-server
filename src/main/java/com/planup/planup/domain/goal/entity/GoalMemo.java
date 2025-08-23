package com.planup.planup.domain.goal.entity;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Getter
@SuperBuilder
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "goal_type")
public class GoalMemo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String memo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usergoal_id")
    private UserGoal userGoal;
    private LocalDate memoDate;

    public void updateMemo(String newMemo) {
        if (newMemo == null) {
            throw new IllegalArgumentException("메모 내용은 null일 수 없습니다.");
        }

        String trimmedMemo = newMemo.trim();
        if (trimmedMemo.length() > 50) {
            throw new IllegalArgumentException("메모는 50자 이내로 작성해주세요.");
        }

        this.memo = trimmedMemo;
    }
}
