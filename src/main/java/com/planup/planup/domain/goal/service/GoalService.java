package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.goal.dto.GoalRequestDto;
import com.planup.planup.domain.goal.dto.GoalResponseDto;
import com.planup.planup.domain.goal.entity.Enum.GoalCategory;
import com.planup.planup.domain.verification.dto.PhotoVerificationResponseDto;

import java.util.List;

public interface GoalService {
    //Query Service

    //Command Service
    GoalResponseDto.GoalResultDto createGoal(Long userId, GoalRequestDto.CreateGoalDto dto);
    List<GoalResponseDto.MyGoalListDto> getGoalList(Long userId, GoalCategory goalCategory);
    GoalResponseDto.MyGoalDetailDto getMyGoalDetails(Long goalId, Long userId);
    void updatePublicGoal(Long goalId, Long userId);
    void deleteGoal(Long goalId, Long userId);
    GoalRequestDto.CreateGoalDto getGoalInfoToUpdate(Long goalId, Long userId);
    void updateGoal(Long goalId, Long userId, GoalRequestDto.CreateGoalDto dto);
    void updateActiveGoal(Long goalId, Long userId);
    List<PhotoVerificationResponseDto.uploadPhotoResponseDto> getGoalPhotos(Long userId, Long goalId);

    }
