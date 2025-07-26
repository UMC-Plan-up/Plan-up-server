package com.planup.planup.domain.goal.convertor;

import com.planup.planup.domain.goal.dto.ChallengeRequestDTO;
import com.planup.planup.domain.goal.dto.ChallengeResponseDTO;
import com.planup.planup.domain.goal.entity.Challenge;
import com.planup.planup.domain.goal.entity.Enum.GoalCategory;
import com.planup.planup.domain.goal.entity.Enum.Status;
import com.planup.planup.domain.goal.entity.PhotoChallenge;
import com.planup.planup.domain.goal.entity.TimeChallenge;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.user.entity.User;

import java.util.List;
import java.util.stream.Collectors;

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

        List<UserGoal> userGoals = photoChallenge.getUserGoals().stream().filter(userGoal -> userGoal.getStatus().equals(Status.ADMIN)).toList();
        UserGoal userGoal = userGoals.get(0);

        return ChallengeResponseDTO.ChallengeResponseInfo.builder()
                .id(photoChallenge.getId())
                .name(userGoal.getUser().getNickname())
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

        List<UserGoal> userGoals = timeChallenge.getUserGoals().stream().filter(userGoal -> userGoal.getStatus().equals(Status.ADMIN)).toList();
        UserGoal userGoal = userGoals.get(0);

        return ChallengeResponseDTO.ChallengeResponseInfo.builder()
                .id(timeChallenge.getId())
                .name(userGoal.getUser().getNickname())
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
