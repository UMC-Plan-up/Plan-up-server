package com.planup.planup.domain.report.entity;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import com.planup.planup.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(exclude = {"user", "goalReports", "dailyRecords"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(
        name = "weekly_report",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_weekly_report_user_year_month_week",
                        columnNames = {"user_id", "year", "month", "week_number"}
                )
        }
)
public class WeeklyReport extends BaseTimeEntity {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int year;
    private int month;
    @Column(name = "week_number")
    private int weekNumber;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private GoalMessage nextGoalMessage;

    @OneToMany(mappedBy = "weeklyReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoalReport> goalReports = new ArrayList<>();

    @OneToMany(mappedBy = "weeklyReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailyRecord> dailyRecords = new ArrayList<>();

    public void updateGoalMessage(GoalMessage newMessage) {
        this.nextGoalMessage = newMessage;
        this.updatedAt = LocalDateTime.now();
    }
}
