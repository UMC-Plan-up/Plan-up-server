package com.planup.planup.domain.notification.dto;


import com.planup.planup.domain.notification.entity.notification.NotificationGroup;
import com.planup.planup.domain.notification.entity.notification.NotificationType;
import com.planup.planup.domain.notification.entity.notification.TargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

public class NotificationResponseDTO {
    
    public record NotificationDTO(
            
        @Schema(description = "알림 ID", example = "1")
        Long id,

        @Schema(description = "알림 메시지 내용", example = "홍길동님이 챌린지 요청을 보냈습니다.")
        String notificationText,
    
        @Schema(description = "알림 클릭 시 이동할 URL", example = "/challenge/123")
        String url,
    
        @Schema(description = "알림 생성 시간", example = "2026-03-18T12:34:56")
        LocalDateTime createdAt,
    
        @Schema(description = "대상 리소스 ID (게시글, 챌린지 등)", example = "123")
        Long targetId,
    
        @Schema(description = "대상 타입 (POST, COMMENT, USER 등)", example = "POST")
        TargetType targetType,
    
        @Schema(description = "알림 타입 (어떤 상황에서 알림이 발생한 것인지에 대해 )", example = "CHALLENGE_REQUEST")
        NotificationType notificationType,
    
        @Schema(description = "알림 그룹 (SERVICE, ADMIN 등)", example = "FRIEND")
        NotificationGroup group,
    
        @Schema(description = "발신자 ID", example = "10")
        Long senderId,
    
        @Schema(description = "발신자 이름", example = "홍길동")
        String senderName,
    
        @Schema(description = "발신자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
        String senderProfile,
    
        @Schema(description = "추가 정보 (목표 변경 내용 등)", example = "목표 횟수: 3회 → 5회")
        String updatedPartsStr,
    
        @Schema(description = "읽음 여부", example = "false")
        boolean isRead
    ) {}
}