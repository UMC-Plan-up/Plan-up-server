package com.planup.planup.domain.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnblockFriendRequestDTO {
    private Long userId; // 차단 해제를 요청하는 사용자 ID
    private String friendNickname; // 차단 해제할 친구의 닉네임
} 