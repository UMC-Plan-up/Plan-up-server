package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.friend.repository.FriendRepository;
import com.planup.planup.domain.friend.service.FriendService;
import com.planup.planup.domain.goal.convertor.GoalConvertor;
import com.planup.planup.domain.goal.dto.GoalRequestDto;
import com.planup.planup.domain.goal.dto.GoalResponseDto;
import com.planup.planup.domain.goal.entity.Enum.GoalCategory;
import com.planup.planup.domain.goal.entity.Enum.Status;
import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.GoalMemo;
import com.planup.planup.domain.goal.repository.CommentRepository;
import com.planup.planup.domain.goal.repository.GoalMemoRepository;
import com.planup.planup.domain.verification.dto.PhotoVerificationResponseDto;
import com.planup.planup.domain.verification.repository.PhotoVerificationRepository;
import com.planup.planup.domain.verification.repository.TimerVerificationRepository;
import com.planup.planup.domain.verification.entity.PhotoVerification;
import com.planup.planup.domain.verification.entity.TimerVerification;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goal.repository.GoalRepository;
import com.planup.planup.domain.goal.repository.UserGoalRepository;
import com.planup.planup.domain.user.repository.UserRepository;
import com.planup.planup.domain.verification.service.TimerVerificationService;
import lombok.RequiredArgsConstructor;
import com.planup.planup.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService{
    private final GoalRepository goalRepository;
    private final UserGoalRepository userGoalRepository;
    private final UserGoalService userGoalService;
    private final UserRepository userRepository;
    private final TimerVerificationRepository timerVerificationRepository;
    private final PhotoVerificationRepository photoVerificationRepository;
    private final CommentService commentService;
    private final CommentRepository commentRepository;
    private final FriendService friendService;
    private final FriendRepository friendRepository;
    private final GoalMemoRepository goalMemoRepository;
    private final TimerVerificationService timerVerificationService;

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
                .userGoal(savedUserGoal)
                .build();
        photoVerificationRepository.save(photoVerification);

        return GoalConvertor.toGoalResultDto(savedGoal);
    }

    //목표 리스트 조회(목표 생성시 -> 세부 내용 조회X)
    @Transactional(readOnly = true)
    public List<GoalResponseDto.GoalCreateListDto> getGoalList(Long userId, GoalCategory goalCategory) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<GoalResponseDto.GoalCreateListDto> result = new ArrayList<>();

        List<UserGoal> friendGoals = userGoalRepository.findFriendGoalsByCategory(userId, goalCategory);
        for (UserGoal userGoal : friendGoals) {
            User creator = userGoalRepository.findByGoalIdAndStatus(
                    userGoal.getGoal().getId(), Status.ADMIN).getUser();

            int currentParticipants = userGoalRepository.countByGoalId(userGoal.getGoal().getId());
            int remainingSlots = userGoal.getGoal().getLimitFriendCount() - currentParticipants;

            result.add(GoalConvertor.toGoalCreateListDto(userGoal, creator, remainingSlots));
        }

        List<UserGoal> communityGoals = userGoalRepository.findCommunityGoalsByCategory(goalCategory);
        for (UserGoal userGoal : communityGoals) {
            User creator = userGoalRepository.findByGoalIdAndStatus(
                    userGoal.getGoal().getId(), Status.ADMIN).getUser();

            int currentParticipants = userGoalRepository.countByGoalId(userGoal.getGoal().getId());
            int remainingSlots = userGoal.getGoal().getLimitFriendCount() - currentParticipants;

            result.add(GoalConvertor.toGoalCreateListDto(userGoal, creator, remainingSlots));
        }

        return result;
    }

    //내 목표 조회(리스트)
    @Transactional(readOnly = true)
    public List<GoalResponseDto.MyGoalListDto> getMyGoals(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<UserGoal> userGoals = userGoalRepository.findByUserId(userId);

        return userGoals.stream()
                .map(GoalConvertor::toMyGoalListDto)
                .collect(Collectors.toList());
    }

    //친구 목표 조회(리스트)
    @Transactional(readOnly = true)
    public List<GoalResponseDto.FriendGoalListDto> getFriendGoals(Long userId, Long friendsId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        userRepository.findById(friendsId)
                .orElseThrow(() -> new RuntimeException("친구를 찾을 수 없습니다."));
        //친구 관계 검증 필요, 추후 구현
        friendService.isFriend(userId, friendsId);

        List<UserGoal> userGoals = userGoalRepository.findByUserIdAndIsPublicTrue(friendsId);

        return userGoals.stream()
                .map(GoalConvertor::toFriendGoalListDto)
                .collect(Collectors.toList());
    }

    //내 목표 조회(세부 내용 조회)
    @Transactional(readOnly = true)
    public GoalResponseDto.MyGoalDetailDto getMyGoalDetails(Long goalId, Long userId) {
        UserGoal userGoal = userGoalService.getByGoalIdAndUserId(goalId, userId);

        return GoalConvertor.toMyGoalDetailsDto(userGoal);
    }

    //활성화/비활성화
    @Transactional
    public void updateActiveGoal(Long goalId, Long userId) {
        UserGoal userGoal = userGoalService.getByGoalIdAndUserId(goalId, userId);

        userGoal.setActive(!userGoal.isActive());
    }

    //공개/비공개
    @Transactional
    public void updatePublicGoal(Long goalId, Long userId) {
        UserGoal userGoal = userGoalService.getByGoalIdAndUserId(goalId, userId);
        //공개 상태 변경
        userGoal.setPublic();
    }

    //친구 목표 수정(정보 조회)
    @Transactional(readOnly = true)
    public GoalRequestDto.CreateGoalDto getGoalInfoToUpdate(Long goalId, Long userId) {
        Goal goal = findGoalById(goalId);

        Integer goalTime = null;
        if (goal.getVerificationType() == VerificationType.TIMER) {
            UserGoal userGoal = userGoalService.getByGoalIdAndUserId(goalId, userId);
            if (userGoal != null && !userGoal.getTimerVerifications().isEmpty()) {
                goalTime = userGoal.getGoalTime();
            }
        }

        return GoalConvertor.toUpdateGoalDto(goal, goalTime);
    }

    @Transactional
    public void updateGoal(Long goalId, Long userId, GoalRequestDto.CreateGoalDto dto) {
        Goal goal = findGoalById(goalId);

        goal.updateFrom(dto);

        if (dto.getVerificationType() == VerificationType.TIMER && dto.getGoalTime() != null) {
            UserGoal userGoal = userGoalService.getByGoalIdAndUserId(goalId, userId);
            if (userGoal != null) {
                userGoal.setGoalTime(dto.getGoalTime());
            }
        }
    }

    @Override
    @Transactional
    public Goal findGoalById(Long goalId) {
        return goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("목표를 찾을 수 없습니다."));
    }


    //목표 삭제
    @Transactional
    public void deleteGoal(Long goalId, Long userId) {
        Goal goal = findGoalById(goalId);

        UserGoal adminUserGoal = userGoalRepository.findByGoalIdAndStatus(goalId, Status.ADMIN);
        if (adminUserGoal == null) {
            throw new RuntimeException("목표의 관리자를 찾을 수 없습니다.");
        }

        if (!adminUserGoal.getUser().getId().equals(userId)) {
            throw new RuntimeException("목표를 삭제할 권한이 없습니다.");
        }

        List<UserGoal> allUserGoals = userGoalRepository.findByGoalId(goalId);

        userGoalRepository.deleteAll(allUserGoals);

        commentRepository.deleteByGoalId(goalId);

        goalRepository.delete(goal);
    }

    //사진 조회
    @Transactional(readOnly = true)
    public List<PhotoVerificationResponseDto.uploadPhotoResponseDto> getGoalPhotos(Long userId, Long goalId) {
        UserGoal userGoal = userGoalService.getByGoalIdAndUserId(goalId, userId);
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

    //랭킹 조회 서비스
    @Transactional(readOnly = true)
    public List<GoalResponseDto.RankingDto> getGoalRanking(Long goalId) {
        List<UserGoal> userGoals = userGoalRepository.findByGoalIdOrderByVerificationCountDesc(goalId);

        // UserGoal 리스트를 RankingDto 리스트로 변환
        return userGoals.stream()
                .map(GoalConvertor::toRankingDto)
                .collect(Collectors.toList());
    }

    //친구 타이머 현황 api
    @Transactional(readOnly = true)
    public List<GoalResponseDto.FriendTimerStatusDto> getFriendTimerStatus(Long goalId, Long userId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("목표를 찾을 수 없습니다."));

        List<UserGoal> userGoals = userGoalRepository.findByGoalId(goalId);

        return userGoals.stream()
                .map(userGoal -> {
                    User user = userGoal.getUser();
                    String todayTime;

                    if (goal.getVerificationType() == VerificationType.TIMER) {
                        // 타이머인 경우 실제 시간 계산
                        LocalTime totalTime = timerVerificationService.getTodayTotalTime(user.getId(), goalId);
                        todayTime = String.format("%02d:%02d:%02d",
                                totalTime.getHour(),
                                totalTime.getMinute(),
                                totalTime.getSecond());
                    } else {
                        todayTime = "00:00:00";
                    }

                    return GoalConvertor.toFriendTimerStatusDto(user, todayTime, goal.getVerificationType());
                })
                .collect(Collectors.toList());
    }

    //메모 관련 메서드들
    public GoalResponseDto.GoalMemoResponseDto saveMemo(
            Long userId,
            Long goalId,
            GoalRequestDto.CreateMemoRequestDto request) {

        UserGoal userGoal = userGoalRepository.findByGoalIdAndUserId(goalId, userId);

        Optional<GoalMemo> existingMemoOpt = goalMemoRepository
                .findByUserGoalAndMemoDate(userGoal, request.getMemoDate());

        if (existingMemoOpt.isEmpty()) {
            return handleNoExistingMemo(userGoal, request);
        }

        return handleExistingMemo(existingMemoOpt.get(), request);
    }

    private GoalResponseDto.GoalMemoResponseDto handleNoExistingMemo(
            UserGoal userGoal,
            GoalRequestDto.CreateMemoRequestDto request) {

        if (request.hasContent()) {
            GoalMemo newMemo = GoalConvertor.toMemo(
                    userGoal,
                    request.getTrimmedMemo(),
                    request.getMemoDate()
            );

            GoalMemo savedMemo = goalMemoRepository.save(newMemo);

            return GoalConvertor.toCreatedResponse(savedMemo);
        } else {
            return GoalConvertor.toNoChangeResponse(request.getMemoDate());
        }
    }

    private GoalResponseDto.GoalMemoResponseDto handleExistingMemo(
            GoalMemo existingMemo,
            GoalRequestDto.CreateMemoRequestDto request) {

        if (request.hasContent()) {
            String newContent = request.getTrimmedMemo();

            if (existingMemo.getMemo().equals(newContent)) {
                return GoalConvertor.toNoChangeResponse(request.getMemoDate());
            }

            GoalConvertor.updateMemoContent(existingMemo, newContent);
            GoalMemo updatedMemo = goalMemoRepository.save(existingMemo);

            return GoalConvertor.toUpdatedResponse(updatedMemo);
        } else {
            goalMemoRepository.delete(existingMemo);

            return GoalConvertor.toDeletedResponse(request.getMemoDate());
        }
    }

    @Override
    public Goal getGoalById(Long id) {
        return null;
    }
}
