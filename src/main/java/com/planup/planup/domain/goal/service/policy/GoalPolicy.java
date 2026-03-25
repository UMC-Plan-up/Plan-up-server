package com.planup.planup.domain.goal.service.policy;

import com.planup.planup.domain.friend.service.FriendReadService;
import com.planup.planup.domain.friend.service.FriendReadServiceImpl;
import com.planup.planup.domain.goal.service.UserGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoalPolicy {

    private final FriendReadService friendReadService;
    private final UserGoalService userGoalService;

//    public void ensureSendInviteFriendTpGoal(Long userId, Long friendId, Long goalId) {
//        //친구인지
//        friendReadService.isFriend(userId, friendId);
//
//        //이미 해당 Goal에 참여중인지
//        boolean exist = userGoalService.existUserGoal(goalId, userId);
//        if ()
//    }
}
