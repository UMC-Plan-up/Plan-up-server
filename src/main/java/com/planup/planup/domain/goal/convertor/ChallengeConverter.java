package com.planup.planup.domain.goal.convertor;

import com.planup.planup.domain.goal.dto.ChallengeRequestDTO;
import com.planup.planup.domain.goal.dto.ChallengeResponseDTO;
import com.planup.planup.domain.goal.entity.Challenge;
import com.planup.planup.domain.goal.entity.Enum.GoalCategory;
import com.planup.planup.domain.goal.entity.Enum.GoalType;
import com.planup.planup.domain.goal.entity.Enum.Status;
import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import com.planup.planup.domain.goal.entity.TimeChallenge;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class ChallengeConverter {

    public static Challenge toPhotoChallenge(ChallengeRequestDTO.create dto) {
        return Challenge.builder()
                .goalName(dto.goalName())
                .goalAmount(dto.goalAmount())
                .goalCategory(GoalCategory.CHALLENGE)
                .goalType(dto.goalType())
                .oneDose(dto.oneDose())
                .endDate(dto.endDate().toLocalDate())
                .limitFriendCount(1)
                .status(dto.status())
                .penalty(dto.penalty())
                .period(dto.period())
                .frequency(dto.frequency())
                .verificationType(VerificationType.PHOTO)
                .build();
    }

    public static TimeChallenge toTimeChallenge(ChallengeRequestDTO.create dto) {
        return TimeChallenge.builder()
                .goalName(dto.goalName())
                .goalAmount(dto.goalAmount())
                .goalCategory(GoalCategory.CHALLENGE)
                .goalType(dto.goalType())
                .oneDose(dto.oneDose())
                .endDate(dto.endDate().toLocalDate())
                .limitFriendCount(2)
                .status(dto.status())
                .penalty(dto.penalty())
                .targetTime(dto.timeChallenge().targetTime())
                .verificationType(VerificationType.TIMER)
                .build();
    }

    public static Challenge challengeToChallenge(Challenge challenge) {
        return Challenge.builder()
                .goalName(challenge.getGoalName())
                .goalAmount(challenge.getGoalAmount())
                .goalCategory(GoalCategory.CHALLENGE)
                .goalType(challenge.getGoalType())
                .oneDose(challenge.getOneDose())
                .endDate(challenge.getEndDate())
                .limitFriendCount(2)
                .status(challenge.getStatus())
                .penalty(challenge.getPenalty())
                .verificationType(challenge.getVerificationType())
                .build();
    }

    public static ChallengeResponseDTO.ChallengeResponseInfo toChallengeResponseInfoPhotoVer(Challenge photoChallenge) {

       return ChallengeResponseDTO.ChallengeResponseInfo.builder()
               .id(photoChallenge.getId())
               .goalName(photoChallenge.getGoalName())
               .goalAmount(photoChallenge.getGoalAmount())
               .goalCategory(GoalCategory.CHALLENGE)
               .goalType(GoalType.CHALLENGE_PHOTO)
               .oneDose(photoChallenge.getOneDose())
               .endDate(photoChallenge.getEndDate().atStartOfDay())
               .status(photoChallenge.getStatus())
               .penalty(photoChallenge.getPenalty())
               .targetTime(null)
               .verificationType(VerificationType.PHOTO)
               .build();
    }

    public static ChallengeResponseDTO.ChallengeResponseInfo toChallengeResponseInfoTimeVer(TimeChallenge timeChallenge) {

        return ChallengeResponseDTO.ChallengeResponseInfo.builder()
                .id(timeChallenge.getId())
                .goalName(timeChallenge.getGoalName())
                .goalAmount(timeChallenge.getGoalAmount())
                .goalCategory(GoalCategory.CHALLENGE)
                .goalType(GoalType.CHALLENGE_TIME)
                .oneDose(timeChallenge.getOneDose())
                .endDate(timeChallenge.getEndDate().atStartOfDay())
                .status(timeChallenge.getStatus())
                .penalty(timeChallenge.getPenalty())
                .targetTime(timeChallenge.getTargetTime())
                .verificationType(VerificationType.TIMER)
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
