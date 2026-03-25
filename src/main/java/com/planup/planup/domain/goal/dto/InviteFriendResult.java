package com.planup.planup.domain.goal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteFriendResult {

    private List<Long> invited;
    private List<Long> alreadyJoined;
    private List<Long> notFriends;
}
