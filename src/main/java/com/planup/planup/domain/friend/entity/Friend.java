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
@Builder
public class Friend extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id")
    private User friend;

    @Enumerated(EnumType.STRING)
    private FriendStatus status;


    public Long getFriendId(Long myId) {
        if (Objects.equals(user.getId(), myId)) {
            return friend.getId();
        } else if (Objects.equals(friend.getId(), myId)){
            return user.getId();
        } else {
            return null;
        }
    }

    public void setStatus(FriendStatus status) {
        this.status = status;
    }
}
