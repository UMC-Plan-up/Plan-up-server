package com.planup.planup.domain.report.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GoalReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long goalId;

    private Long achievementRate;

    private String goalTitle;
    private String goalCriteria;

    @Embedded
    private ThreeWeekAchievementRate threeWeekAhcievementRate;

    @Embedded
    private DailyAchievementRate dailyAchievementRate;

    @OneToMany(mappedBy = "goalReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReportUser> reportUsers;


    @Enumerated(EnumType.STRING)
    private ReportType reportType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_report_id")
    private WeeklyReport weeklyReport;

    public GoalReport(Long goalId, Long achievementRate, String goalTitle, String goalCriteria, WeeklyReport weeklyReport, ReportType reportType) {
        this.goalId = goalId;
        this.achievementRate = achievementRate;
        this.goalTitle = goalTitle;
        this.goalCriteria = goalCriteria;
        this.weeklyReport = weeklyReport;
        this.reportType = reportType;
    }
}
