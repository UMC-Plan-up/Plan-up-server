package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoAuthResponseDTO {
    private boolean isNewUser;
    private String tempUserId;   // 신규 사용자인 경우
    private String accessToken;  // 기존 사용자인 경우
    private UserInfoResponseDTO userInfo; // 기존 사용자인 경우
}