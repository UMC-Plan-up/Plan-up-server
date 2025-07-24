package com.planup.planup.domain.notification.message;

import com.planup.planup.domain.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class NotificationMessageProvider {

    public String generate(NotificationType type, String senderName, String receiverName) {
        return switch (type) {
            case CHALLENGE_REQUEST_SENT ->
                    String.format("[%s]님에게 챌린지 참여 요청을 보냈어요.", receiverName);
            case CHALLENGE_REQUEST_RECEIVED ->
                    String.format("[%s]님이 챌린지 참여를 요청했어요.", senderName);
            case CHALLENGE_REQUEST_ACCEPTED ->
                    String.format("[%s]님이 챌린지 참여를 수락했어요.", receiverName);
            case CHALLENGE_REQUEST_REJECTED ->
                    String.format("[%s]님이 챌린지 참여를 거절했어요.", receiverName);
            case PENALTY_PROPOSAL_SENT ->
                    String.format("[%s]님에게 패널티를 제안했어요.", receiverName);
            case PENALTY_PROPOSAL_RECEIVED ->
                    String.format("[%s]님이 패널티를 제안했어요.", senderName);
            case PENALTY_PROPOSAL_SENT_RE ->
                    String.format("[%s]님에게 새로운 패널티를 제안했어요.", receiverName);
            case PENALTY_PROPOSAL_RECEIVED_RE ->
                    String.format("[%s]님이 새로운 패널티를 제안했어요.", senderName);
            case PENALTY_ACCEPTED ->
                    String.format("[%s]님이 패널티 제안을 수락했어요.", senderName);
            case PENALTY_REJECTED ->
                    String.format("[%s]님이 패널티 제안을 거절했어요. 다른 친구에게 참여를 요청해 보세요.", senderName);
            case PENALTY_REJECTED_CHALLENGE_CANCELLED ->
                    String.format("[%s]님이 패널티 제안을 거절했어요. [%s]님과의 챌린지가 취소되었어요.", senderName, senderName);
        };
    }
}
