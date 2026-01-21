package com.planup.planup.domain.friend.entity.reportEntity;


import com.planup.planup.domain.global.entity.BaseTimeEntity;
import com.planup.planup.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
@Builder
@Table(
        name = "user_report_mapping",
        indexes = {
                @Index(name = "idx_reporter", columnList = "reporter_id"),
                @Index(name = "idx_reported", columnList = "reported_id"),
                @Index(name = "idx_reporter_reported", columnList = "reporter_id, reported_id"),
                @Index(name = "idx_reported_status", columnList = "reported_id, status")
        }
)
public class UserReportMapping extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reported_id", nullable = false)
    private User reported;

    private String reason;

    private boolean blocked;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    private LocalDateTime handledAt;

    public void handleReport(ReportStatus status) {
        this.status = status;
        handledAt = LocalDateTime.now();
    }
}
