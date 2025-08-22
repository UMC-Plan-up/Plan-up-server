package com.planup.planup.domain.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoLinkResponseDTO {
    private boolean success;
    private String message;
    private String kakaoEmail;
    private UserInfoResponseDTO userInfo;
}
