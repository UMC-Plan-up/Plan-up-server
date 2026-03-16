package com.planup.planup.domain.goal.convertor;

import com.planup.planup.domain.goal.dto.ChallengeRequestDTO;
import com.planup.planup.domain.goal.dto.ChallengeResponseDTO;
import com.planup.planup.domain.goal.entity.Challenge;
import com.planup.planup.domain.goal.entity.Enum.GoalCategory;
import com.planup.planup.domain.goal.entity.Enum.GoalType;
import com.planup.planup.domain.goal.entity.Enum.VerificationType;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class ChallengeConverter {

    public static Challenge toChallenge(ChallengeRequestDTO.create dto, VerificationType verificationType) {
        return Challenge.builder()
                .goalName(dto.goalName())
                .goalAmount(dto.goalAmount())
                .goalCategory(GoalCategory.CHALLENGE)
                .goalType(dto.goalType())
                .oneDose(dto.oneDose())
                .endDate(dto.endDate().toLocalDate())
                .limitFriendCount(2)
                .status(dto.status())
                .penalty(dto.penalty())
                .referencePeriod(dto.referencePeriod())
                .frequency(dto.frequency())
                .verificationType(verificationType)
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

    public static ChallengeResponseDTO.ChallengeResponseInfo toChallengeResponseInfoPhotoVer(Challenge challenge) {

       return ChallengeResponseDTO.ChallengeResponseInfo.builder()
               .id(challenge.getId())
               .goalName(challenge.getGoalName())
               .goalAmount(challenge.getGoalAmount())
               .goalCategory(GoalCategory.CHALLENGE)
               .goalType(GoalType.CHALLENGE_PHOTO)
               .oneDose(challenge.getOneDose())
               .endDate(challenge.getEndDate().atStartOfDay())
               .status(challenge.getStatus())
               .penalty(challenge.getPenalty())
               .frequency(challenge.getFrequency())
               .verificationType(VerificationType.PHOTO)
               .referencePeriod(challenge.getReferencePeriod())
               .build();
    }

    public static ChallengeResponseDTO.ChallengeResponseInfo toChallengeResponseInfoTimeVer(Challenge challenge) {

        return ChallengeResponseDTO.ChallengeResponseInfo.builder()
                .id(challenge.getId())
                .goalName(challenge.getGoalName())
                .goalAmount(challenge.getGoalAmount())
                .goalCategory(GoalCategory.CHALLENGE)
                .goalType(GoalType.CHALLENGE_TIME)
                .oneDose(challenge.getOneDose())
                .endDate(challenge.getEndDate().atStartOfDay())
                .status(challenge.getStatus())
                .penalty(challenge.getPenalty())
                .frequency(challenge.getFrequency())
                .verificationType(VerificationType.TIMER)
                .referencePeriod(challenge.getReferencePeriod())
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
