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
public class UserReportMapping extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_id")
    private User reported;

    private String reason;

    private boolean blocked;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    private LocalDateTime handledAt;


}
