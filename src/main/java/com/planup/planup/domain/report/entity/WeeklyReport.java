package com.planup.planup.domain.report.entity;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import com.planup.planup.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int year;
    private Long weekNumber;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private GoalMessage nextGoalMessage;

    @OneToMany(mappedBy = "weeklyReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoalReport> goalReports = new ArrayList<>();

    @OneToMany(mappedBy = "weeklyReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailyRecord> dailyRecords = new ArrayList<>();

    @Builder
    public WeeklyReport(User user, int year, Long weekNumber, LocalDateTime startDate, LocalDateTime endDate, GoalMessage nextGoalMessage) {
        this.user = user;
        this.year = year;
        this.weekNumber = weekNumber;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.nextGoalMessage = nextGoalMessage;
    }

    public void updateGoalMessage(GoalMessage newMessage) {
        this.nextGoalMessage = newMessage;
        this.updatedAt = LocalDateTime.now();
    }
}
