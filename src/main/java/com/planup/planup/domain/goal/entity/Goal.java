package com.planup.planup.domain.goal.entity;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import com.planup.planup.domain.goal.entity.Enum.GoalCategory;
import com.planup.planup.domain.goal.entity.Enum.GoalPeriod;
import com.planup.planup.domain.goal.entity.Enum.GoalType;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@SuperBuilder
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "goal_type")
public class Goal extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //제목
    private String goalName;
    //전체 목표량
    private String goalAmount;

    //목표 종류(독서, 공부 등등)
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private GoalCategory goalCategory;

    //목표 타입(친구, 커뮤니티, 챌린지)
    @Enumerated(EnumType.STRING)
    @Column(length = 20, name = "goal_type_enum")
    private GoalType goalType;

    //목표 빈도 기간(하루, 1주, 1달/ 하루에 __, 1주일에 __하기)
    @Enumerated(EnumType.STRING)
    private GoalPeriod period;

    @OneToMany(mappedBy = "goal")
    private List<UserGoal> userGoals;

    //빈도
    private int frequency;
    //1회 분량//이거 수정
    private int oneDose;
    //종료일
    private Date endDate;

    //목표 인증 방식(타이머/사진)
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private VerificationType verificationType;

    //친구수 제한
    private int limitFriendCount;

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> commentList = new ArrayList<>();

    public boolean isChallenge() {
        if (this.getGoalType().equals(GoalType.CHALLENGE_PHOTO) || this.getGoalType().equals(GoalType.CHALLENGE_TIME)) {
            return true;
        }
        return false;
    }
}
