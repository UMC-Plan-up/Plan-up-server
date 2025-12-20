package com.planup.planup.domain.reaction.domain;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import com.planup.planup.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "reaction",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reaction_user_target_type",
                        columnNames = {"user_id", "target_type", "target_id", "reaction_type"}
                )
        },
        indexes = {
                @Index(name = "idx_reaction_target", columnList = "target_type, target_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class Reaction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false, length = 20)
    private ReactionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private ReactionTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;
}
