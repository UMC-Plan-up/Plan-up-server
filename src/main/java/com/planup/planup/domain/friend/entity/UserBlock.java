package com.planup.planup.domain.friend.entity;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import com.planup.planup.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name="user_block", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"blocker_id", "blocked_id", "active"})
})
@Getter
@SuperBuilder
@SQLDelete(sql = "UPDATE user_block SET active = false, deleted_at = NOW() WHERE id = ?")   //soft delete
@Where(clause = "active = 1") // 조회 시 활성 행만 자동 필터링
@AllArgsConstructor
@NoArgsConstructor
public class UserBlock extends BaseTimeEntity {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "blocker_id", nullable = false)
    private User blocker;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "blocked_id", nullable = false)
    private User blocked;

    @Column(nullable = false)
    private boolean active = true;

    private LocalDateTime deletedAt;
}
