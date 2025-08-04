package com.planup.planup.domain.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockedFriendResponseDTO {
    private Long friendId; // 차단된 친구의 ID
    private String friendNickname; // 차단된 친구의 닉네임
} 