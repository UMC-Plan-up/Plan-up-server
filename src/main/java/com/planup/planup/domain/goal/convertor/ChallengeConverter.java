package com.planup.planup.domain.goal.convertor;

import com.planup.planup.domain.goal.dto.ChallengeRequestDTO;
import com.planup.planup.domain.goal.dto.ChallengeResponseDTO;
import com.planup.planup.domain.goal.entity.Challenge;
import com.planup.planup.domain.goal.entity.Enum.GoalCategory;
import com.planup.planup.domain.goal.entity.Enum.Status;
import com.planup.planup.domain.goal.entity.TimeChallenge;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.user.entity.User;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ChallengeConverter {

    public static Challenge toPhotoChallenge(ChallengeRequestDTO.create dto) {
        return Challenge.builder()
                .goalName(dto.goalName())
                .goalAmount(dto.goalAmount())
                .goalCategory(GoalCategory.CHALLENGE)
                .goalType(dto.goalType())
                .oneDose(dto.oneDose())
                .endDate(convertToDate(dto.endDate()))
                .limitFriendCount(1)
                .status(dto.status())
                .penalty(dto.penalty())
                .period(dto.period())
                .frequency(dto.frequency())
                .build();
    }

    public static TimeChallenge toTimeChallenge(ChallengeRequestDTO.create dto) {
        return TimeChallenge.builder()
                .goalName(dto.goalName())
                .goalAmount(dto.goalAmount())
                .goalCategory(GoalCategory.CHALLENGE)
                .goalType(dto.goalType())
                .oneDose(dto.oneDose())
                .endDate(convertToDate(dto.endDate()))
                .limitFriendCount(1)
                .status(dto.status())
                .penalty(dto.penalty())
                .targetTime(dto.timeChallenge().targetTime())
                .build();
    }

    public static ChallengeResponseDTO.ChallengeResponseInfo toChallengeResponseInfoPhotoVer(Challenge photoChallenge) {

        List<UserGoal> userGoals = photoChallenge.getUserGoals().stream().filter(userGoal -> userGoal.getStatus().equals(Status.ADMIN)).toList();
        UserGoal userGoal = userGoals.get(0);

        return ChallengeResponseDTO.ChallengeResponseInfo.builder()
                .id(photoChallenge.getId())
                .name(userGoal.getUser().getNickname())
                .goalName(photoChallenge.getGoalName())
                .goalType(photoChallenge.getGoalType())
                .goalAmount(photoChallenge.getGoalAmount())
                .endDate(convertToLocalDateTime(photoChallenge.getEndDate()))
                .period(photoChallenge.getPeriod())
                .frequency(photoChallenge.getFrequency())
                .targetTime(null)
                .build();
    }

    public static ChallengeResponseDTO.ChallengeResponseInfo toChallengeResponseInfoTimeVer(TimeChallenge timeChallenge) {

        List<UserGoal> userGoals = timeChallenge.getUserGoals().stream().filter(userGoal -> userGoal.getStatus().equals(Status.ADMIN)).toList();
        UserGoal userGoal = userGoals.get(0);

        return ChallengeResponseDTO.ChallengeResponseInfo.builder()
                .id(timeChallenge.getId())
                .name(userGoal.getUser().getNickname())
                .goalName(timeChallenge.getGoalName())
                .goalType(timeChallenge.getGoalType())
                .goalAmount(timeChallenge.getGoalAmount())
                .endDate(convertToLocalDateTime(timeChallenge.getEndDate()))
                .period(timeChallenge.getPeriod())
                .frequency(timeChallenge.getFrequency())
                .targetTime(timeChallenge.getTargetTime())
                .build();
    }

    public static LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static Date convertToDate(LocalDateTime localDateTime) {
        return Date.from(
                localDateTime.atZone(ZoneId.systemDefault()).toInstant()
        );
    }
}
