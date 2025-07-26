package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.goal.convertor.GoalConvertor;
import com.planup.planup.domain.goal.dto.GoalRequestDto;
import com.planup.planup.domain.goal.dto.GoalResponseDto;
import com.planup.planup.domain.goal.entity.Comment;
import com.planup.planup.domain.goal.entity.Enum.Status;
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
                .map(userGoal -> userGoalRepository.countByGoalIdAndIsActiveTrue(userGoal.getGoal().getId()))
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

    //내 목표 조회(세부 내용 조회)
    @Transactional
    public GoalResponseDto.MyGoalDetailDto updateActiveGoal(Long goalId, Long userId) {
        UserGoal userGoal = userGoalRepository.findByGoalIdAndUserId(goalId, userId);

        //활성화 상태 변경
        userGoal.setActive(!userGoal.isActive());

        //오늘 기록 시간 조회
        LocalTime todayTime = timerVerificationService.getTodayTotalTime(userGoal.getId());
        //댓글 조회
        List<Comment> commentList = commentService.getComments(goalId);

        return GoalConvertor.toMyGoalDetailsDto(userGoal, todayTime, commentList);
    }
}
