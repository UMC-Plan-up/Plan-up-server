package com.planup.planup.domain.goal.convertor;

import com.planup.planup.domain.goal.dto.GoalRequestDto;
import com.planup.planup.domain.goal.dto.GoalResponseDto;
import com.planup.planup.domain.goal.entity.Comment;
import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.TimerVerification;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.user.entity.User;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GoalConvertor {

    //목표 생성 Dto를 받아 goal 엔터티에 저장하도록 변환
    public static Goal toGoal(GoalRequestDto.CreateGoalDto createGoalDto) {
        return Goal.builder()
                .goalName(createGoalDto.getGoalName())
                .goalAmount(createGoalDto.getGoalAmount())
                .goalCategory(createGoalDto.getGoalCategory())
                .goalType(createGoalDto.getGoalType())
                .oneDose(createGoalDto.getOneDose())
                .endDate(createGoalDto.getEndDate())
                .period(createGoalDto.getPeriod())
                .verificationType(createGoalDto.getVerificationType())
                .frequency(createGoalDto.getFrequency())
                .limitFriendCount(createGoalDto.getLimitFriendCount())
                .build();
    }

    public static GoalResponseDto.GoalResultDto toGoalResultDto(Goal goal) {
        return GoalResponseDto.GoalResultDto.builder()
                .goalId(goal.getId())
                .goalName(goal.getGoalName())
                .goalAmount(goal.getGoalAmount())
                .goalCategory(goal.getGoalCategory())
                .goalType(goal.getGoalType())
                .oneDose(goal.getOneDose())
                .period(goal.getPeriod())
                .frequency(goal.getFrequency())
                .endDate(goal.getEndDate())
                .verificationType(goal.getVerificationType())
                .limitFriendCount(goal.getLimitFriendCount())
                .build();
    }

    //내 목표 리스트 조회 변환(DTO 가져오기)
    public static GoalResponseDto.MyGoalListDto toMyGoalListDto(
            UserGoal userGoal,
            User creator,
            int participantCount) {

        Goal goal = userGoal.getGoal();

        Integer goalTime = null;

        if (goal.getVerificationType() == VerificationType.TIMER) {
            if (!userGoal.getTimerVerifications().isEmpty()) {
                TimerVerification timerVerification = userGoal.getTimerVerifications().get(0);
                goalTime = timerVerification.getGoalTime();
            }
        }

        return GoalResponseDto.MyGoalListDto.builder()
                .goalId(goal.getId())
                .goalName(goal.getGoalName())
                .goalCategory(goal.getGoalCategory())
                .goalType(goal.getGoalType())
                .verificationType(goal.getVerificationType())
                .goalTime(goalTime)
                .frequency(goal.getFrequency())
                .oneDose(goal.getOneDose())
                .creatorNickname(creator.getNickname())
                .creatorProfileImg(creator.getProfileImg())
                .myStatus(userGoal.getStatus())
                .participantCount(participantCount)
                .isActive(userGoal.isActive())
                .build();
    }

    //내 목표 리스트 조회 변환(DTO -> List 변환)
    public static List<GoalResponseDto.MyGoalListDto> toMyGoalListDtoList(
            List<UserGoal> userGoals,
            List<User> creators,
            List<Integer> participantCounts) {

        return IntStream.range(0, userGoals.size())
                .mapToObj(i -> toMyGoalListDto(
                        userGoals.get(i),
                        creators.get(i),
                        participantCounts.get(i)
                ))
                .collect(Collectors.toList());
    }

    //세부 목표 조회 DTO 변환
    public static GoalResponseDto.MyGoalDetailDto toMyGoalDetailsDto(
            UserGoal userGoal,
            LocalTime todayTime,
            List<Comment> commentList) {

        Goal goal = userGoal.getGoal();

        return GoalResponseDto.MyGoalDetailDto.builder()
                .goalId(goal.getId())
                .goalName(goal.getGoalName())
                .oneDose(goal.getOneDose())
                .isActive(userGoal.isActive())
                .todayTime(todayTime)
                .commentList(commentList)
                .build();
    }

    //목표 수정을 위해 기존 정보 조회 DTO 변환
    public static GoalRequestDto.CreateGoalDto toUpdateGoalDto(Goal goal, Integer goalTime) {
        return GoalRequestDto.CreateGoalDto.builder()
                .goalName(goal.getGoalName())
                .goalAmount(goal.getGoalAmount())
                .goalCategory(goal.getGoalCategory())
                .goalType(goal.getGoalType())
                .oneDose(goal.getOneDose())
                .frequency(goal.getFrequency())
                .period(goal.getPeriod())
                .endDate(goal.getEndDate())
                .verificationType(goal.getVerificationType())
                .limitFriendCount(goal.getLimitFriendCount())
                .goalTime(goalTime)
                .build();
    }
}
