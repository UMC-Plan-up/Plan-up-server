package com.planup.planup.domain.goal.convertor;

import com.planup.planup.domain.goal.dto.GoalRequestDto;
import com.planup.planup.domain.goal.dto.GoalResponseDto;
import com.planup.planup.domain.goal.entity.Comment;
import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.GoalMemo;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.user.entity.User;

import java.time.LocalDate;
import java.util.List;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.Collectors;

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

    //목표 리스트 조회 변환(DTO 가져오기)
    public static GoalResponseDto.GoalCreateListDto toGoalCreateListDto(
            UserGoal userGoal,
            User creator,
            int participantCount) {

        Goal goal = userGoal.getGoal();

        Integer goalTime = null;

        if (goal.getVerificationType() == VerificationType.TIMER) {
            if (!userGoal.getTimerVerifications().isEmpty()) {
                goalTime = userGoal.getGoalTime();
            }
        }

        return GoalResponseDto.GoalCreateListDto.builder()
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
                .participantCount(participantCount)
                .build();
    }

    //내 목표 리스트 조회 변환(DTO 가져오기)
    public static GoalResponseDto.MyGoalListDto toMyGoalListDto(
            UserGoal userGoal) {
        Goal goal = userGoal.getGoal();

        return GoalResponseDto.MyGoalListDto.builder()
                .goalId(goal.getId())
                .goalName(goal.getGoalName())
                .goalType(goal.getGoalType())
                .frequency(goal.getFrequency())
                .oneDose(goal.getOneDose())
                .build();
    }

    //친구 목표 리스트 조회 변환(DTO 가져오기)
    public static GoalResponseDto.FriendGoalListDto toFriendGoalListDto(
            UserGoal friendGoal) {
        Goal goal = friendGoal.getGoal();

        return GoalResponseDto.FriendGoalListDto.builder()
                .goalId(goal.getId())
                .goalName(goal.getGoalName())
                .goalAmount(goal.getGoalAmount())
                .goalPeriod(goal.getPeriod())
                .goalType(goal.getGoalType())
                .verificationType(goal.getVerificationType())
                .goalTime(friendGoal.getGoalTime())
                .frequency(goal.getFrequency())
                .oneDose(goal.getOneDose())
                .build();
    }

    //세부 목표 조회 DTO 변환
    public static GoalResponseDto.MyGoalDetailDto toMyGoalDetailsDto(
            UserGoal userGoal) {

        Goal goal = userGoal.getGoal();

        return GoalResponseDto.MyGoalDetailDto.builder()
                .goalId(goal.getId())
                .goalName(goal.getGoalName())
                .oneDose(goal.getOneDose())
                .isPublic(userGoal.isPublic())
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

    //랭킹 변환
    public static GoalResponseDto.RankingDto toRankingDto(UserGoal userGoal) {
        return GoalResponseDto.RankingDto.builder()
                .goalId(userGoal.getGoal().getId())
                .userId(userGoal.getUser().getId())
                .nickName(userGoal.getUser().getNickname())
                .profileImg(userGoal.getUser().getProfileImg())
                .verificationCount(userGoal.getVerificationCount())
                .build();
    }

    //친구 타이머 현황 DTO 변환
    public static GoalResponseDto.FriendTimerStatusDto toFriendTimerStatusDto(
            User user,
            String todayTime,
            VerificationType verificationType) {

        return GoalResponseDto.FriendTimerStatusDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImg(user.getProfileImg())
                .todayTime(todayTime)
                .verificationType(verificationType)
                .build();
    }

    //메모관련 컨버터들
    public static GoalResponseDto.GoalMemoResponseDto toCreatedResponse(GoalMemo memo) {
        return GoalResponseDto.GoalMemoResponseDto.builder()
                .action("CREATED")
                .memoId(memo.getId())
                .memo(memo.getMemo())
                .memoDate(memo.getMemoDate())
                .message("메모가 성공적으로 생성되었습니다.")
                .build();
    }

    public static GoalResponseDto.GoalMemoResponseDto toUpdatedResponse(GoalMemo memo) {
        return GoalResponseDto.GoalMemoResponseDto.builder()
                .action("UPDATED")
                .memoId(memo.getId())
                .memo(memo.getMemo())
                .memoDate(memo.getMemoDate())
                .message("메모가 성공적으로 수정되었습니다.")
                .build();
    }

    public static GoalResponseDto.GoalMemoResponseDto toDeletedResponse(LocalDate memoDate) {
        return GoalResponseDto.GoalMemoResponseDto.builder()
                .action("DELETED")
                .memoId(null)
                .memo(null)
                .memoDate(memoDate)
                .message("메모가 성공적으로 삭제되었습니다.")
                .build();
    }

    public static GoalResponseDto.GoalMemoResponseDto toNoChangeResponse(LocalDate memoDate) {
        return GoalResponseDto.GoalMemoResponseDto.builder()
                .action("NO_CHANGE")
                .memoId(null)
                .memo(null)
                .memoDate(memoDate)
                .message("변경사항이 없습니다.")
                .build();
    }

    public static GoalMemo toMemo(UserGoal userGoal, String memo, LocalDate memoDate) {
        return GoalMemo.builder()
                .userGoal(userGoal)
                .memo(memo.trim())
                .memoDate(memoDate)
                .build();
    }

    public static GoalResponseDto.DailyVerifiedGoalsResponse toDailyVerifiedGoalsResponse(
            LocalDate date, List<Goal> verifiedGoals) {

        List<GoalResponseDto.VerifiedGoalInfo> goalInfoList = verifiedGoals.stream()
                .map(GoalConvertor::toVerifiedGoalInfo)
                .collect(Collectors.toList());

        return GoalResponseDto.DailyVerifiedGoalsResponse.builder()
                .date(date)
                .verifiedGoals(goalInfoList)
                .totalCount(goalInfoList.size())
                .build();
    }

    public static GoalResponseDto.VerifiedGoalInfo toVerifiedGoalInfo(Goal goal) {
        return GoalResponseDto.VerifiedGoalInfo.builder()
                .goalName(goal.getGoalName())
                .period(goal.getPeriod())
                .frequency(goal.getFrequency())
                .build();
    }

    public static GoalResponseDto.DailyAchievementDto toDailyAchievementDto(
            LocalDate date,
            int achievementRate) {

        return GoalResponseDto.DailyAchievementDto.builder()
                .date(date)
                .achievementRate(achievementRate)
                .build();
    }

    public static void updateMemoContent(GoalMemo existingMemo, String newMemo) {
        existingMemo.updateMemo(newMemo.trim());
    }

    public static LocalDateTime convertToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public static Date convertToDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
