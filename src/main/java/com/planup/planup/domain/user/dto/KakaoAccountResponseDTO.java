package com.planup.planup.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoAccountResponseDTO {
    private boolean isLinked; // 카카오톡 계정 연동 여부
    private String kakaoEmail; // 연동된 카카오톡 이메일 (연동되지 않은 경우 null)
} 
