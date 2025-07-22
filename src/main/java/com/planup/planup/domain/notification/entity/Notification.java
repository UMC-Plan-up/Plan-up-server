package com.planup.planup.domain.notification.entity;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import com.planup.planup.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Notification extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String content;

    @Enumerated(EnumType.STRING)
    private TargetType targetType; // POST, COMMENT, USER 등

    //서비스 단에서 URL 제작해서 넘겨주기
    //유저가 유저에게 넘겨주는 알림 같은 경우에만 URL 제공 나머지 시스템에서 처리하는 경우는 URL 제외
    private Long targetId;

    private boolean isRead;

    public static Notification create(User sender, User receiver, NotificationType type, String content, TargetType targetType, Long targetId) {
        Notification n = new Notification();
        n.sender = sender;
        n.receiver = receiver;
        n.type = type;
        n.content = content;
        n.targetType = targetType;
        n.targetId = targetId;
        n.isRead = false;
        return n;
    }

    public void markAsRead(boolean isRead) {
        this.isRead = isRead;
    }
}
