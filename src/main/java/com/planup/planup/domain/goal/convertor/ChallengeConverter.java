package com.planup.planup.domain.goal.convertor;

import com.planup.planup.domain.goal.dto.ChallengeRequestDTO;
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
                .rePenalty(dto.rePenalty())
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
                .rePenalty(dto.rePenalty())
                .targetTime(dto.timeChallenge().getTargetTime())
                .build();
    }
}
