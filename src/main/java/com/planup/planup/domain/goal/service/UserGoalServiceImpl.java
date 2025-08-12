package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.friend.service.FriendService;
import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.friend.entity.FriendStatus;
import com.planup.planup.domain.friend.repository.FriendRepository;
import com.planup.planup.domain.goal.dto.CommunityResponseDto;
import com.planup.planup.domain.goal.convertor.UserGoalConvertor;
import com.planup.planup.domain.goal.entity.Enum.GoalType;
import com.planup.planup.domain.goal.entity.Enum.Status;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goal.repository.GoalRepository;
import com.planup.planup.domain.goal.repository.UserGoalRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserGoalServiceImpl implements UserGoalService{

    private final UserGoalRepository userGoalRepository;
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final FriendService friendService;

    @Transactional
    public CommunityResponseDto.JoinGoalResponseDto joinGoal(Long userId, Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 목표입니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));

        UserGoal existingUserGoal = userGoalRepository.findByGoalIdAndUserId(goalId, userId);
        if (existingUserGoal != null) {
            throw new RuntimeException("이미 참가한 목표입니다.");
        }

        if (goal.getGoalType() == GoalType.FRIEND) {
            validateFriendRelation(userId, goalId);
        }

        int currentParticipants = userGoalRepository.countByGoalId(goalId);
        if (currentParticipants >= goal.getLimitFriendCount()) {
            throw new RuntimeException("목표 정원이 초과되었습니다.");
        }

        if (goal.getEndDate() != null && goal.getEndDate().before(new Date())) {
            throw new RuntimeException("이미 종료된 목표입니다.");
        }

        UserGoal userGoal = UserGoal.builder()
                .user(user)
                .goal(goal)
                .status(Status.MEMBER)
                .isActive(true)
                .isPublic(true)
                .currentAmount(null)
                .verificationCount(0)
                .goalTime(0)
                .build();

        UserGoal savedUserGoal = userGoalRepository.save(userGoal);

        return UserGoalConvertor.toJoinGoalResponseDto(savedUserGoal, goal, user);
    }

    //친구 관계 검증을 위한 헬퍼 메서드
    private void validateFriendRelation(Long userId, Long goalId) {
        UserGoal adminUserGoal = userGoalRepository.findByGoalIdAndStatus(goalId, Status.ADMIN);
        if (adminUserGoal == null) {
            throw new RuntimeException("목표 생성자를 찾을 수 없습니다.");
        }

        Long creatorId = adminUserGoal.getUser().getId();

        friendService.isFriend(userId, creatorId);

    }

    //수용 형 파트
    @Override
    @Transactional(readOnly = true)
    public UserGoal getUserGoalByUserAndGoal(User user, Goal goal) {
        return userGoalRepository.findAllByUserAndGoal(user, goal).get(0);
//        return userGoalRepository.findByUserAndGoal(user, goal).orElseThrow(() -> new UserGoalException(ErrorStatus.NOT_FOUND_USERGOAL));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserGoal> getUserGoalListByGoal(Goal goal) {
        return userGoalRepository.findAllByGoal(goal);
    }

    @Override
    @Transactional(readOnly = true)
    public VerificationType checkVerificationType(UserGoal userGoal) {
        return userGoal.getGoal().getVerificationType();
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserGoal> getUserGoalInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return userGoalRepository.findAllByUpdatedAtBetween(startDate, endDate);
    }
}
