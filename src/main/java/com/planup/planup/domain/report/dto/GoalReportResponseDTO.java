package com.planup.planup.domain.report.dto;

import com.planup.planup.domain.report.entity.ReportType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class GoalReportResponseDTO {

    @Schema(description = "목표별 리포트 응답 DTO")
    public record GoalReportResponse(

            @Schema(description = "목표 리포트 ID", example = "1")
            Long id,

            @Schema(description = "목표 ID", example = "101")
            Long goalId,

            @Schema(description = "목표 제목", example = "하루 3시간 공부하기")
            String goalTitle,

            @Schema(description = "목표 기준", example = "일일 최소 3시간")
            String goalCriteria,

            @Schema(description = "목표 달성률", example = "85")
            Long achievementRate,

            @Schema(description = "리포트 유형", example = "MY")
            ReportType reportType,

            @Schema(description = "3주간 성취율")
            ThreeWeekAchievementRateResponse threeWeekAchievementRate,

            @Schema(description = "요일별 성취율")
            DailyAchievementRateResponse dailyAchievementRate,

            @Schema(description = "사용자별 성취율 리스트")
            List<ReportUserResponse> reportUsers
    ) {
    }

    @Schema(description = "3주간 성취율 응답 DTO")
    public record ThreeWeekAchievementRateResponse(

            @Schema(description = "이번 주 성취율", example = "85")
            int thisWeek,

            @Schema(description = "1주 전 성취율", example = "78")
            int oneWeekBefore,

            @Schema(description = "2주 전 성취율", example = "65")
            int twoWeekBefore
    ) {
    }

    @Schema(description = "요일별 성취율 응답 DTO")
    public record DailyAchievementRateResponse(

            @Schema(description = "월요일", example = "70") int mon,
            @Schema(description = "화요일", example = "80") int tue,
            @Schema(description = "수요일", example = "90") int wed,
            @Schema(description = "목요일", example = "85") int thu,
            @Schema(description = "금요일", example = "95") int fri,
            @Schema(description = "토요일", example = "75") int sat,
            @Schema(description = "일요일", example = "65") int sun,
            @Schema(description = "총합 또는 평균 성취율", example = "570") int total
    ) {
    }

    @Schema(description = "리포트 사용자 성취율 응답 DTO")
    public record ReportUserResponse(

            @Schema(description = "친구/참여자의 id", example = "1")
            Long id,

            @Schema(description = "친구/참여자의 이름", example = "홍길동")
            String userName,

            @Schema(description = "친구/참여자의 성취율", example = "90")
            int rate
    ) {
    }
}
