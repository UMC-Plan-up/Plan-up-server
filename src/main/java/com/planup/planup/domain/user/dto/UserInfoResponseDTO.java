package com.planup.planup.domain.user.dto;

import com.planup.planup.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponseDTO {
    private Long id;
    private String email;
    private String nickname;
    private String profileImg;
    private Boolean serviceNotificationAllow; // 서비스 알림 동의 상태
    private Boolean marketingNotificationAllow; // 혜택 및 마케팅 알림 동의 상태

    public static UserInfoResponseDTO from(User user) {
        return UserInfoResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImg(user.getProfileImg())
                .serviceNotificationAllow(user.getServiceNotificationAllow())
                .marketingNotificationAllow(user.getMarketingNotificationAllow())
                .build();
    }
}