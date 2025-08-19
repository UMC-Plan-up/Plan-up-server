package com.planup.planup.domain.report.converter;

import com.planup.planup.domain.goal.dto.CommentResponseDto;
import com.planup.planup.domain.report.entity.DailyAchievementRate;
import com.planup.planup.domain.report.entity.GoalReport;
import com.planup.planup.domain.report.entity.ReportUser;
import com.planup.planup.domain.report.entity.ThreeWeekAchievementRate;

import java.util.List;
import java.util.stream.Collectors;

import static com.planup.planup.domain.report.dto.GoalReportResponseDTO.*;

public class GoalReportConverter {

    public static GoalReportResponse toResponse(GoalReport entity, List<CommentResponseDto.CommentDto> commentDtoList) {
        return new GoalReportResponse(
                entity.getId(),
                entity.getGoalId(),
                entity.getGoalTitle(),
                entity.getGoalCriteria(),
                (long) entity.getDailyAchievementRate().getTotal(),
                entity.getReportType(),
                toThreeWeekAchievementRateResponse(entity.getThreeWeekAhcievementRate()),
                toDailyAchievementRateResponse(entity.getDailyAchievementRate()),
                toReportUserResponseList(entity.getReportUsers()),
                commentDtoList
        );
    }

    private static ThreeWeekAchievementRateResponse toThreeWeekAchievementRateResponse(ThreeWeekAchievementRate rate) {
        if (rate == null) return null;

        return new ThreeWeekAchievementRateResponse(
                rate.getThisWeek(),
                rate.getOneWeekBefore(),
                rate.getTwoWeekBefore()
        );
    }

    private static DailyAchievementRateResponse toDailyAchievementRateResponse(DailyAchievementRate rate) {
        if (rate == null) return null;

        return new DailyAchievementRateResponse(
                rate.getMon(), rate.getTue(), rate.getWed(),
                rate.getThu(), rate.getFri(), rate.getSat(),
                rate.getSun(), rate.getTotal()
        );
    }

    private static List<ReportUserResponse> toReportUserResponseList(List<ReportUser> users) {
        if (users == null) return List.of();

        return users.stream()
                .map(user -> new ReportUserResponse(
                        user.getId(),
                        user.getUserName(),
                        user.getRate()
                ))
                .collect(Collectors.toList());
    }
}
