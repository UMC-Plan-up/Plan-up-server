package com.planup.planup.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponseDTO {
    private Long id;
    private String email;
    private String nickname;
    private String profileImg;
    // 필요하다면 추가 필드
}