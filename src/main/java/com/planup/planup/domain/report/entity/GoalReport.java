package com.planup.planup.domain.report.entity;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class GoalReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long goalId;
    private Long userId;

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

    public void setReportUsers(List<ReportUser> reportUsers) {
        this.reportUsers = reportUsers;
    }

}
