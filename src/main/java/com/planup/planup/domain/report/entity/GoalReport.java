package com.planup.planup.domain.report.entity;

import com.planup.planup.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoalReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long goalId;

    private Long achievementRate;

    private String goalTitle;
    private String goalCriteria;

    @Embedded
    private ThreeWeekAhcievementRate threeWeekAhcievementRate;

    @Embedded
    private DailyAchievementRate dailyAchievementRate;

    @OneToMany(mappedBy = "goalReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReportUser> reportUsers;


    @Enumerated(EnumType.STRING)
    private ReportType reportType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_report_id")
    private WeeklyReport weeklyReport;

    @Builder
    public GoalReport(Long goalId, Long achievementRate, String goalTitle, String goalCriteria, WeeklyReport weeklyReport, ReportType reportType) {
        this.goalId = goalId;
        this.achievementRate = achievementRate;
        this.goalTitle = goalTitle;
        this.goalCriteria = goalCriteria;
        this.weeklyReport = weeklyReport;
        this.reportType = reportType;
    }
}
