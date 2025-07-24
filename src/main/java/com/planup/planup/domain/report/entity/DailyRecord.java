package com.planup.planup.domain.report.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime date;
    private int recordedTime;

    @Lob
    private String photoVerified;

    private String simpleMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_report_id")
    private WeeklyReport weeklyReport;

    @Builder
    public DailyRecord(LocalDateTime date, int recordedTime, String photoVerified, WeeklyReport weeklyReport, String simpleMessage) {
        this.date = date;
        this.recordedTime = recordedTime;
        this.photoVerified = photoVerified;
        this.weeklyReport = weeklyReport;
        this.simpleMessage = simpleMessage;
    }
}