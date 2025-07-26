package com.planup.planup.domain.goal.convertor;

import com.planup.planup.domain.goal.dto.ChallengeRequestDTO;
import com.planup.planup.domain.goal.dto.ChallengeResponseDTO;
import com.planup.planup.domain.goal.entity.Challenge;
import com.planup.planup.domain.goal.entity.Enum.GoalCategory;
import com.planup.planup.domain.goal.entity.PhotoChallenge;
import com.planup.planup.domain.goal.entity.TimeChallenge;

public class ChallengeConverter {

    public static PhotoChallenge toPhotoChallenge(ChallengeRequestDTO.create dto) {
        return PhotoChallenge.builder()
                .goalName(dto.goalName())
                .goalAmount(dto.goalAmount())
                .goalCategory(GoalCategory.CHALLENGE)
                .goalType(dto.goalType())
                .oneDose(dto.oneDose())
                .endDate(dto.endDate())
                .limitFriendCount(1)
                .status(dto.status())
                .penalty(dto.penalty())
                .timePerPeriod(dto.photoChallenge().timePerPeriod())
                .frequency(dto.photoChallenge().frequency())
                .build();
    }

    public static TimeChallenge toTimeChallenge(ChallengeRequestDTO.create dto) {
        return TimeChallenge.builder()
                .goalName(dto.goalName())
                .goalAmount(dto.goalAmount())
                .goalCategory(GoalCategory.CHALLENGE)
                .goalType(dto.goalType())
                .oneDose(dto.oneDose())
                .endDate(dto.endDate())
                .limitFriendCount(1)
                .status(dto.status())
                .penalty(dto.penalty())
                .targetTime(dto.timeChallenge().getTargetTime())
                .build();
    }

    public static ChallengeResponseDTO.ChallengeResponseInfo toChallengeResponseInfoPhotoVer(PhotoChallenge photoChallenge) {
        return ChallengeResponseDTO.ChallengeResponseInfo.builder()
                .id(photoChallenge.getId())
                .name(photoChallenge.getU)
                .goalName(photoChallenge.getGoalName())
                .goalType(photoChallenge.getGoalType())
                .goalAmount(photoChallenge.getGoalAmount())
                .endDate(photoChallenge.getEndDate())
                .timePerPeriod(photoChallenge.getTimePerPeriod())
                .frequency(photoChallenge.getFrequency())
                .targetTime(null)
                .build();
    }

    public static ChallengeResponseDTO.ChallengeResponseInfo toChallengeResponseInfoTimeVer(TimeChallenge timeChallenge) {
        return ChallengeResponseDTO.ChallengeResponseInfo.builder()
                .id(timeChallenge.getId())
                .goalName(timeChallenge.getGoalName())
                .goalType(timeChallenge.getGoalType())
                .goalAmount(timeChallenge.getGoalAmount())
                .endDate(timeChallenge.getEndDate())
                .timePerPeriod(0)
                .frequency(0)
                .targetTime(timeChallenge.getTargetTime())
                .build();
    }
}
