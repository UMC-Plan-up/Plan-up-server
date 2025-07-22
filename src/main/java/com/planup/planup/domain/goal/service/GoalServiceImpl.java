package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.goal.convertor.GoalConvertor;
import com.planup.planup.domain.goal.dto.GoalRequestDto;
import com.planup.planup.domain.goal.dto.GoalResponseDto;
import com.planup.planup.domain.goal.entity.Enum.Status;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goal.repository.GoalRepository;
import com.planup.planup.domain.goal.repository.UserGoalRepository;
import com.planup.planup.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import com.planup.planup.domain.user.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService{
    private final GoalRepository goalRepository;
    private final UserGoalRepository userGoalRepository;
    private final UserRepository userRepository;

    public GoalResponseDto.GoalResultDto createGoal(Long userId, GoalRequestDto.CreateGoalDto createGoalDto){
        Goal goal = GoalConvertor.toGoal(createGoalDto);
        Goal savedGoal = goalRepository.save(goal);

        UserGoal userGoal = UserGoal.builder()
                .user(User.builder().id(userId).build())
                .goal(savedGoal)
                .status(Status.ADMIN)
                .isActive(true)
                .build();
        userGoalRepository.save(userGoal);

        return GoalConvertor.toGoalResultDto(savedGoal);
    }

    public List<GoalResponseDto.MyGoalListDto> getMyGoals(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<UserGoal> userGoals = userGoalRepository.findByUserIdAndIsActiveTrue(userId);

        List<User> creators = userGoals.stream()
                .map(userGoal -> {
                    Long goalId = userGoal.getGoal().getId();
                    return userGoalRepository.findByGoalIdAndStatus(goalId, Status.ADMIN)
                            .getUser();
                })
                .collect(Collectors.toList());

        List<Integer> participantCounts = userGoals.stream()
                .map(userGoal -> userGoalRepository.countByGoalIdAndIsActiveTrue(userGoal.getGoal().getId()))
                .collect(Collectors.toList());

        return GoalConvertor.toMyGoalListDtoList(userGoals, creators, participantCounts);
    }
}
