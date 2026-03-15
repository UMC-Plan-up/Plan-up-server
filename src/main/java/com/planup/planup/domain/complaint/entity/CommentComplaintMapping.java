package com.planup.planup.domain.complaint.entity;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import com.planup.planup.domain.goal.entity.Comment;
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
        name = "comment_complaint_mapping",
        indexes = {
                @Index(name = "idx_comment_complaint_reporter_comment", columnList = "reporter_id, comment_id"),
                @Index(name = "idx_comment_complaint_comment", columnList = "comment_id")
        }
)
public class CommentComplaintMapping extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SanctionDetailReason reason;
}
