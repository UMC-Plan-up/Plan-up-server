package com.planup.planup.domain.goal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "초대에 성공한 경우")
    private List<Long> invited;
    @Schema(description = "이미 해당 goal에 참여중인 경우: 예외처리")
    private List<Long> alreadyJoined;
    @Schema(description = "친구 관계가 아닌 경우: 예외처리")
    private List<Long> notFriends;
}
