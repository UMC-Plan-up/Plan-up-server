package com.planup.planup.domain.friend.service.policy;

import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FriendSelector {

    public List<User> pickCounterpartUsers(User me, List<Friend> relations) {
        return relations.stream()
                .map(f -> f.getFriend().equals(me) ? f.getUser() : f.getFriend())
                .distinct()
                .toList();
    }
}
