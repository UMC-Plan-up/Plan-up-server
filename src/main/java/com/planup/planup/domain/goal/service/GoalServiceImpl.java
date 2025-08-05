package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.goal.dto.convertor.GoalConvertor;
import com.planup.planup.domain.goal.dto.GoalRequestDto;
import com.planup.planup.domain.goal.dto.GoalResponseDto;
import com.planup.planup.domain.goal.entity.Comment;
import com.planup.planup.domain.goal.entity.Enum.Status;
import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.repository.CommentRepository;
import com.planup.planup.domain.user.verification.repository.PhotoVerificationRepository;
import com.planup.planup.domain.user.verification.repository.TimerVerificationRepository;
import com.planup.planup.domain.user.verification.service.TimerVerificationService;
import com.planup.planup.domain.user.verification.entity.PhotoVerification;
import com.planup.planup.domain.user.verification.entity.TimerVerification;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goal.repository.GoalRepository;
import com.planup.planup.domain.goal.repository.UserGoalRepository;
import com.planup.planup.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import com.planup.planup.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
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
    private final CommentRepository commentRepository;

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
                .verificationCount(0)
                .build();
        UserGoal savedUserGoal = userGoalRepository.save(userGoal);

        //Refactor : 인증 테이블 자동 생성
        TimerVerification timerVerification = TimerVerification.builder()
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

    //목표 리스트 조회(세부 내용 조회X)
    @Transactional(readOnly = true)
    public List<GoalResponseDto.MyGoalListDto> getGoalList(Long userId, GoalCategory goalCategory) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<GoalResponseDto.MyGoalListDto> result = new ArrayList<>();

        List<UserGoal> friendGoals = userGoalRepository.findFriendGoalsByCategory(userId, goalCategory);
        for (UserGoal userGoal : friendGoals) {
            User creator = userGoalRepository.findByGoalIdAndStatus(
                    userGoal.getGoal().getId(), Status.ADMIN).getUser();

            int currentParticipants = userGoalRepository.countByGoalId(userGoal.getGoal().getId());
            int remainingSlots = userGoal.getGoal().getLimitFriendCount() - currentParticipants;

            result.add(GoalConvertor.toMyGoalListDto(userGoal, creator, remainingSlots));
        }

        List<UserGoal> communityGoals = userGoalRepository.findCommunityGoalsByCategory(goalCategory);
        for (UserGoal userGoal : communityGoals) {
            User creator = userGoalRepository.findByGoalIdAndStatus(
                    userGoal.getGoal().getId(), Status.ADMIN).getUser();

            int currentParticipants = userGoalRepository.countByGoalId(userGoal.getGoal().getId());
            int remainingSlots = userGoal.getGoal().getLimitFriendCount() - currentParticipants;

            result.add(GoalConvertor.toMyGoalListDto(userGoal, creator, remainingSlots));
        }

        return result;
    }

    //내 목표 조회(세부 내용 조회)
    @Transactional(readOnly = true)
    public GoalResponseDto.MyGoalDetailDto getMyGoalDetails(Long goalId, Long userId) {
        UserGoal userGoal = userGoalRepository.findByGoalIdAndUserId(goalId, userId);

        //댓글 조회
        List<Comment> commentList = commentService.getComments(goalId);

        return GoalConvertor.toMyGoalDetailsDto(userGoal, commentList);
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
                goalTime = userGoal.getGoalTime();
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
                userGoal.setGoalTime(dto.getGoalTime());
            }
        }
    }

    //목표 삭제
    @Transactional
    public void deleteGoal(Long goalId, Long userId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("목표를 찾을 수 없습니다."));

        UserGoal adminUserGoal = userGoalRepository.findByGoalIdAndStatus(goalId, Status.ADMIN);
        if (adminUserGoal == null) {
            throw new RuntimeException("목표의 관리자를 찾을 수 없습니다.");
        }

        if (!adminUserGoal.getUser().getId().equals(userId)) {
            throw new RuntimeException("목표를 삭제할 권한이 없습니다.");
        }

        List<UserGoal> allUserGoals = userGoalRepository.findByGoalId(goalId);

        for (UserGoal userGoal : allUserGoals) {
            userGoalRepository.delete(userGoal);
        }

        commentRepository.deleteByGoalId(goalId);

        // 5. 마지막으로 Goal 삭제
        goalRepository.delete(goal);
    }

    //사진 조회
    @Transactional(readOnly = true)
    public List<PhotoVerificationResponseDto.uploadPhotoResponseDto> getGoalPhotos(Long userId, Long goalId) {
        UserGoal userGoal = userGoalRepository.findByGoalIdAndUserId(goalId, userId);
        if (userGoal == null) {
            throw new RuntimeException("해당 목표를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        return userGoal.getPhotoVerifications().stream()
                .map(verification -> PhotoVerificationResponseDto.uploadPhotoResponseDto.builder()
                        .verificationId(verification.getId())
                        .goalId(goalId)
                        .photoImg(verification.getPhotoImg())
                        .build())
                .collect(Collectors.toList());
    }
}
