package com.planup.planup.domain.friend.entity;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import com.planup.planup.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
@Table(
        name = "friend",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_friend_pair",
                        columnNames = {"user_low_id", "user_high_id"}
                )
        },
        indexes = {
                @Index(name = "idx_friend_pair", columnList = "user_low_id,user_high_id")
        }
)
public class Friend extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "friend_id", nullable = false)
    private User friend;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private FriendStatus status = FriendStatus.REQUESTED;


    public Long getFriendId(Long myId) {
        return getFriendNotMe(myId).getId();
    }

    public User getFriendNotMe(Long myId) {
        if (Objects.equals(user.getId(), myId)) {
            return friend;
        } else if (Objects.equals(friend.getId(), myId)){
            return user;
        } else {
            return null;
        }
    }

    public void setStatus(FriendStatus status) {
        this.status = status;
    }
}
