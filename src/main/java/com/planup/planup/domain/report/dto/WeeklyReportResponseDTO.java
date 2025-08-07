package com.planup.planup.domain.report.dto;

import com.planup.planup.domain.bedge.dto.BadgeResponseDTO;
import com.planup.planup.domain.bedge.entity.BadgeType;
import com.planup.planup.domain.report.entity.DailyAchievementRate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class WeeklyReportResponseDTO {

    @Builder
    public record achievementResponse(
            @Schema(description = "뱃지 기록")
            List<badgeDTO> bedgeDTOList,

            @Schema(description = "친구 알림")
            List<NotificationDTO> notificationDTOList,

            @Schema(description = "응원 메시지")
            String cheering
            ) {
    }

    @Builder
    public record badgeDTO(
            @Schema(description = "뱃지 타입")
            BadgeType badgeType,

            @Schema(description = "뱃지 이름", example = "영향력 있는 시작")
            String badgeName
    ) {

    }

    @Builder
    public record NotificationDTO(
            @Schema(description = "알림 아이디", example = "1")
            Long id,

            @Schema(description = "알림 내용", example = "친구 1님의 댓글 '벌써 이만큼 함?'")
            String notificationText
    ) {}

    @Builder
    public record existWeek(
            @Schema(description = "존재하는 주차 리포트가 있는 주", example = "{1,2,3}")
            List<Integer> weeks
    ) {}


    @Builder
    public record WeeklyReportResponse(
            @Schema(description = "주간 리포트 ID", example = "1")
            Long id,

            @Schema(description = "연도", example = "2025")
            int year,

            @Schema(description = "월", example = "7")
            int month,

            @Schema(description = "주차", example = "3")
            int weekNumber,

            @Schema(description = "시작 날짜", example = "2025-07-14T00:00:00")
            LocalDateTime startDate,

            @Schema(description = "종료 날짜", example = "2025-07-20T23:59:59")
            LocalDateTime endDate,

            @Schema(description = "다음 목표에 대한 메시지", example = "다음 주도 힘내자!")
            String nextGoalMessage,

            @Schema(description = "목표별 간단 리포트 리스트")
            List<SimpleGoalReport> goalReports,

            @Schema(description = "요일별 성취율 총합", example = "450")
            DailyAchievementRate totalDailyAchievement,

            @Schema(description = "일자별 기록")
            List<DailyRecordDTO> dailyRecordList,

            @Schema(description = "이번 주 받은 활동 배지")
            List<BadgeResponseDTO.SimpleBadgeDTO> badgeList
    ) {
        @Builder
        @Schema(description = "간단한 목표 리포트 DTO")
        public record SimpleGoalReport(

                @Schema(description = "goal Id", example = "1")
                Long id,

                @Schema(description = "목표 제목", example = "하루 3시간 공부하기")
                String goalTitle,

                @Schema(description = "목표 기준", example = "일일 최소 3시간")
                String goalCriteria,

                @Schema(description = "목표 달성률", example = "85")
                int achievementRate
        ) {}

        @Builder
        @Schema(description = "일자별 기록 간단 버전")
        public record DailyRecordDTO(
                @Schema(description = "기록 ID", example = "1")
                Long id,

                @Schema(description = "기록한 날짜 및 시간", example = "2024-07-01T09:00:00")
                LocalDateTime date,

                @Schema(description = "기록된 시간(분)", example = "150")
                long recordedTime,

                @Schema(description = "사진 인증 (Base64 인코딩 이미지)", example = "data:image/png;base64,iVBORw0KGgoAAAANS...")
                String photoVerified,

                @Schema(description = "간단한 메시지", example = "오늘은 열심히 공부했다!")
                String simpleMessage
        ) {}
    }
}
