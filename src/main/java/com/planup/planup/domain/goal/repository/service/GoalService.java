package com.planup.planup.domain.goal.repository.service;

import com.planup.planup.domain.goal.dto.GoalRequestDto;
import com.planup.planup.domain.goal.dto.GoalResponseDto;
import com.planup.planup.domain.goal.entity.Enum.GoalCategory;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.verification.dto.PhotoVerificationResponseDto;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface GoalService {
    //Query Service

    //Command Service
    GoalResponseDto.GoalResultDto createGoal(Long userId, GoalRequestDto.CreateGoalDto dto);
    List<GoalResponseDto.GoalCreateListDto> getFriendGoalsByCategory(Long userId, GoalCategory goalCategory);
    List<GoalResponseDto.GoalCreateListDto> getCommunityGoalsByCategory(GoalCategory goalCategory);

    GoalResponseDto.MyGoalDetailDto getMyGoalDetails(Long goalId, Long userId);
    void updatePublicGoal(Long goalId, Long userId);

    @Transactional
    Goal findGoalById(Long goalId);

    void deleteGoal(Long goalId, Long userId);
    GoalRequestDto.CreateGoalDto getGoalInfoToUpdate(Long goalId, Long userId);
    void updateGoal(Long goalId, Long userId, GoalRequestDto.CreateGoalDto dto);
    void updateActiveGoal(Long goalId, Long userId);
    List<PhotoVerificationResponseDto.uploadPhotoResponseDto> getGoalPhotos(Long userId, Long goalId);
    List<GoalResponseDto.MyGoalListDto> getMyGoals(Long userId);
    List<GoalResponseDto.FriendGoalListDto> getFriendGoals(Long userId, Long friendsId);
    List<GoalResponseDto.RankingDto> getGoalRanking(Long goalId);
    List<GoalResponseDto.FriendTimerStatusDto> getFriendTimerStatus(Long goalId, Long userId);
    GoalResponseDto.GoalMemoResponseDto saveMemo(Long userId, Long goalId, GoalRequestDto.CreateMemoRequestDto request);
    GoalResponseDto.DailyVerifiedGoalsResponse getDailyVerifiedGoals(Long userId, LocalDate date);
    GoalResponseDto.GoalMemoReadDto getMemo(Long userId, Long goalId, LocalDate date);
    List<GoalResponseDto.GoalMemoReadDto> getMemosByPeriod(Long userId, Long goalId, LocalDate startDate, LocalDate endDate);
    GoalResponseDto.GoalReactionDto getGoalReactions(Long goalId, Long userId);
    GoalResponseDto.ReactionResultDto addCheer(Long goalId, Long userId);
    GoalResponseDto.ReactionResultDto addEncourage(Long goalId, Long userId);

    Goal getGoalById(Long challengeId);

    @Transactional(readOnly = true)
    List<User> getOtherMember(User user, Goal goal);
}
