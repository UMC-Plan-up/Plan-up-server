package com.planup.planup.domain.complaint.entity;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import com.planup.planup.domain.goalphoto.entity.GoalPhoto;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.enums.SanctionDetailReason;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "photo_complaint_mapping",
        indexes = {
                @Index(name = "idx_photo_complaint_reporter_photo", columnList = "reporter_id, photo_id"),
                @Index(name = "idx_photo_complaint_photo", columnList = "photo_id")
        }
)
public class PhotoComplaintMapping extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "photo_id", nullable = false)
    private GoalPhoto photo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SanctionDetailReason reason;
}
