package com.planup.planup.domain.notification.message;

import com.planup.planup.domain.global.SpringContext;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.service.GoalService;
import com.planup.planup.domain.notification.entity.Notification;
import com.planup.planup.domain.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

public class NotificationMessageProvider {

    public static String generate(Notification notification) {

        GoalService goalService = SpringContext.getBean(GoalService.class);

        String senderName = notification.getSender().getNickname();
        String receiverName = notification.getReceiver().getNickname();
        Long targetId = notification.getTargetId();
        String updatedPartsStr = notification.getNotificationMessage();

        String goalName = null;
        if (isTargetId(notification)) {
            Goal goal = goalService.getGoalById(targetId);
            goalName = goal.getGoalName();
        }

        return switch (notification.getType()) {

            case RANK_DOWN ->
                    String.format("[%s]님의 랭킹 자리를 뺏겼어요!", receiverName);
            case GOAL_REMINDER ->
                    String.format("[%s]을(를) 실천할 시간이에요! 지금 바로 기록하세요!", goalName);
            case FRIEND_GOAL_COMPLETED ->
                    String.format("[%s]님이 기준 기간 내에 '%s'을(를) 완료했어요.", senderName, goalName);
            case FRIEND_GOAL_CREATED ->
                    String.format("[%s]님이 새 목표 '%s'을(를) 추가했어요.", senderName, goalName);
            case GOAL_PART_UPDATED -> {
                // targetId는 goalId로 쓰고, 수정된 파트 목록을 context에서 받아옴

                yield String.format("[%s]님이 '%s' 목표의 [%s]을(를) 수정했어요.\n목표 세부 내역 확인하기",
                        senderName, goalName, updatedPartsStr);
            }

            case COMMENT_ON_VERIFICATION ->
                    String.format("[%s]님이 댓글을 달았어요. ", senderName);

            case GOAL_CHEERED ->
                    String.format("[%s]님이 [%s] 목표에 ‘응원해요’ 버튼을 눌렀어요.", senderName, goalName);

            case GOAL_ENCOURAGED ->
                    String.format("[%s]님이 [%s] 목표에 ‘분발해요’ 버튼을 눌렀어요.", senderName, goalName);

            case FRIEND_REQUEST_SENT ->
                    String.format("[%s]님이 친구 신청을 보냈어요.", senderName);

            case FRIEND_REQUEST_ACCEPTED ->
                    String.format("[%s]님과 친구가 되었어요.", senderName);

            case FRIEND_REQUEST_REJECTED ->
                    String.format("[%s]님이 친구 신청을 수락하지 않았어요.", senderName);

            case CHALLENGE_REQUEST_SENT ->
                    String.format("[%s]님에게 챌린지 참여 요청을 보냈어요.", receiverName);
            case CHALLENGE_REQUEST_RECEIVED ->
                    String.format("[%s]님이 챌린지 참여를 요청했어요.", senderName);
            case CHALLENGE_REQUEST_ACCEPTED ->
                    String.format("[%s]님이 챌린지 참여를 수락했어요.", receiverName);
            case CHALLENGE_REQUEST_REJECTED ->
                    String.format("[%s]님이 챌린지 참여를 거절했어요.", receiverName);

            case PENALTY_PROPOSAL_SENT ->
                    String.format("[%s]님에게 새로운 패널티를 제안했어요.", receiverName);
            case PENALTY_PROPOSAL_RECEIVED ->
                    String.format("[%s]님이 새로운 패널티를 제안했어요.", senderName);
            case PENALTY_ACCEPTED ->
                    String.format("[%s]님의 패널티 제안을 수락했어요.", receiverName);
            case PENALTY_REJECTED ->
                    String.format("[%s]님의 패널티 제안을 거절했어요. 다른 친구에게 참여를 요청해 보세요.", receiverName);
            case CHALLENGE_STARTED ->
                    String.format("[%s]님과의 챌린지가 시작되었어요! [%s]님보다 빨리 목표를 기록해 보세요.", receiverName, receiverName);
            case CHALLENGE_ENDED ->
                    String.format("[%s]님과의 챌린지가 종료되었어요. 챌린지 결과를 확인해 보세요!", receiverName);
            case PENALTY_REMINDER_SENT ->
                    String.format("[%s]님이 패널티 리마인드를 보냈어요! 챌린지 결과를 자세히 확인해 보세요.", senderName);
        };
    }

    private static boolean isTargetId(Notification notification) {
        NotificationType type = notification.getType();

        if (type == NotificationType.GOAL_ENCOURAGED ||
                type == NotificationType.GOAL_CHEERED ||
                type == NotificationType.GOAL_REMINDER ||
                type == NotificationType.FRIEND_GOAL_COMPLETED ||
                type == NotificationType.FRIEND_GOAL_CREATED) {
            return true;
        }
        return false;
    }
}
