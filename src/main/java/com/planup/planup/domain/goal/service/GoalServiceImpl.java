package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.goal.convertor.GoalConvertor;
import com.planup.planup.domain.goal.dto.GoalRequestDto;
import com.planup.planup.domain.goal.dto.GoalResponseDto;
import com.planup.planup.domain.goal.entity.Comment;
import com.planup.planup.domain.goal.entity.Enum.Status;
import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.PhotoVerification;
import com.planup.planup.domain.goal.entity.TimerVerification;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goal.repository.GoalRepository;
import com.planup.planup.domain.goal.repository.PhotoVerificationRepository;
import com.planup.planup.domain.goal.repository.TimerVerificationRepository;
import com.planup.planup.domain.goal.repository.UserGoalRepository;
import com.planup.planup.domain.goal.service.verification.TimerVerificationService;
import com.planup.planup.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import com.planup.planup.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService{
    private final GoalRepository goalRepository;
    private final UserGoalRepository userGoalRepository;
    private final UserRepository userRepository;
    private final TimerVerificationRepository timerVerificationRepository;
    private final PhotoVerificationRepository photoVerificationRepository;
    private final TimerVerificationService timerVerificationService;
    private final CommentService commentService;

    //목표 생성
    @Transactional
    public GoalResponseDto.GoalResultDto createGoal(Long userId, GoalRequestDto.CreateGoalDto createGoalDto){
        Goal goal = GoalConvertor.toGoal(createGoalDto);
        Goal savedGoal = goalRepository.save(goal);

        UserGoal userGoal = UserGoal.builder()
                .user(User.builder().id(userId).build())
                .goal(savedGoal)
                .status(Status.ADMIN)
                .currentAmount(null)
                .isActive(true)
                .isPublic(true)
                .build();
        UserGoal savedUserGoal = userGoalRepository.save(userGoal);

        //Refactor : 인증 테이블 자동 생성
        TimerVerification timerVerification = TimerVerification.builder()
                .goalTime(createGoalDto.getGoalTime() != null ? createGoalDto.getGoalTime() : 0)
                .spentTime(Duration.ZERO)
                .userGoal(savedUserGoal)
                .build();
        timerVerificationRepository.save(timerVerification);

        PhotoVerification photoVerification = PhotoVerification.builder()
                .photoImg(null)
                .photoDescription(null)
                .userGoal(savedUserGoal)
                .build();
        photoVerificationRepository.save(photoVerification);

        return GoalConvertor.toGoalResultDto(savedGoal);
    }

    //내 목표 리스트 조회(세부 내용 조회X)
    @Transactional(readOnly = true)
    public List<GoalResponseDto.MyGoalListDto> getMyGoals(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        //N+1 문제 성능 개선
        List<UserGoal> userGoals = userGoalRepository.findByUserIdAndIsActiveTrueWithVerifications(userId);

        List<User> creators = userGoals.stream()
                .map(userGoal -> {
                    Long goalId = userGoal.getGoal().getId();
                    return userGoalRepository.findByGoalIdAndStatus(goalId, Status.ADMIN)
                            .getUser();
                })
                .collect(Collectors.toList());

        List<Integer> participantCounts = userGoals.stream()
                .map(userGoal -> userGoalRepository.countByGoalIdAndActiveTrue(userGoal.getGoal().getId()))
                .collect(Collectors.toList());

        return GoalConvertor.toMyGoalListDtoList(userGoals, creators, participantCounts);
    }

    //내 목표 조회(세부 내용 조회)
    @Transactional(readOnly = true)
    public GoalResponseDto.MyGoalDetailDto getMyGoalDetails(Long goalId, Long userId) {
        UserGoal userGoal = userGoalRepository.findByGoalIdAndUserId(goalId, userId);

        //오늘 기록 시간 조회
        LocalTime todayTime = timerVerificationService.getTodayTotalTime(userGoal.getId());

        //댓글 조회
        List<Comment> commentList = commentService.getComments(goalId);

        return GoalConvertor.toMyGoalDetailsDto(userGoal, todayTime, commentList);
    }

    //활성화/비활성화
    @Transactional
    public void updateActiveGoal(Long goalId, Long userId) {
        UserGoal userGoal = userGoalRepository.findByGoalIdAndUserId(goalId, userId);

        userGoal.setActive(!userGoal.isActive());
    }

    //공개/비공개
    @Transactional
    public void updatePublicGoal(Long goalId, Long userId) {
        UserGoal userGoal = userGoalRepository.findByGoalIdAndUserId(goalId, userId);
        //공개 상태 변경
        userGoal.setPublic(!userGoal.isPublic());
    }

    //친구 목표 수정(정보 조회)
    @Transactional(readOnly = true)
    public GoalRequestDto.CreateGoalDto getGoalInfoToUpdate(Long goalId, Long userId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("목표를 찾을 수 없습니다."));

        Integer goalTime = null;
        if (goal.getVerificationType() == VerificationType.TIMER) {
            UserGoal userGoal = userGoalRepository.findByGoalIdAndUserId(goalId, userId);
            if (userGoal != null && !userGoal.getTimerVerifications().isEmpty()) {
                goalTime = userGoal.getTimerVerifications().get(0).getGoalTime();
            }
        }

        return GoalConvertor.toUpdateGoalDto(goal, goalTime);
    }

    @Transactional
    public void updateGoal(Long goalId, Long userId, GoalRequestDto.CreateGoalDto dto) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("목표를 찾을 수 없습니다."));

        goal.setGoalName(dto.getGoalName());
        goal.setGoalAmount(dto.getGoalAmount());
        goal.setGoalCategory(dto.getGoalCategory());
        goal.setGoalType(dto.getGoalType());
        goal.setOneDose(dto.getOneDose());
        goal.setFrequency(dto.getFrequency());
        goal.setPeriod(dto.getPeriod());
        goal.setEndDate(dto.getEndDate());
        goal.setVerificationType(dto.getVerificationType());
        goal.setLimitFriendCount(dto.getLimitFriendCount());

        if (dto.getVerificationType() == VerificationType.TIMER && dto.getGoalTime() != null) {
            UserGoal userGoal = userGoalRepository.findByGoalIdAndUserId(goalId, userId);
            if (userGoal != null) {
                // Repository 쿼리로 한 번에 업데이트
                timerVerificationRepository.updateGoalTimeByUserGoalId(dto.getGoalTime(), userGoal.getId());
            }
        }
    }

    //목표 삭제
    @Transactional
    public void deleteGoal(Long goalId, Long userId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("목표를 찾을 수 없습니다."));

        UserGoal userGoal = userGoalRepository.findByGoalIdAndStatus(goalId, Status.ADMIN);
        if (!userGoal.getUser().getId().equals(userId)) {
            throw new RuntimeException("목표를 삭제할 권한이 없습니다.");
        }

        goalRepository.delete(goal);
    }
}
