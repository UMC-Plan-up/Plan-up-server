package com.planup.planup.domain.goal.service;

import com.planup.planup.apiPayload.exception.custom.GoalException;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.friend.repository.FriendRepository;
import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.ChallengeException;
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
import com.planup.planup.domain.notification.service.NotificationCreateService;
import com.planup.planup.domain.notification.service.NotificationService;
import com.planup.planup.domain.user.entity.UserLevel;
import com.planup.planup.domain.user.service.UserService;
import com.planup.planup.domain.verification.dto.PhotoVerificationResponseDto;
import com.planup.planup.domain.verification.repository.PhotoVerificationRepository;
import com.planup.planup.domain.verification.repository.TimerVerificationRepository;
import com.planup.planup.domain.verification.entity.PhotoVerification;
import com.planup.planup.domain.verification.entity.TimerVerification;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goal.repository.GoalRepository;
import com.planup.planup.domain.goal.repository.UserGoalRepository;
import com.planup.planup.domain.user.repository.UserRepository;
import com.planup.planup.domain.verification.service.TimerVerificationReadService;
import com.planup.planup.domain.verification.service.TimerVerificationService;
import lombok.RequiredArgsConstructor;
import com.planup.planup.domain.user.entity.User;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
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
    @Lazy
    private final TimerVerificationService timerVerificationService;
    private final TimerVerificationReadService timerVerificationReadService;
    private final UserService userService;
    private final NotificationCreateService notificationCreateService;
    //목표 생성
    @Transactional
    public GoalResponseDto.GoalResultDto createGoal(Long userId, GoalRequestDto.CreateGoalDto createGoalDto){
        //목표 제목 에러 처리
        validateGoalName(createGoalDto.getGoalName(), userId);
        //유저 검증
        User user = userService.getUserbyUserId(userId);
        //레벨 별 목표 생성 제한
        validateGoalCreationLimit(user);
        //종료일 에러 처리
        validateEndDate(createGoalDto.getEndDate());

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

        notificationCreateService.createGoalCreatedNotification(userId, goal.getId());

        return GoalConvertor.toGoalResultDto(savedGoal);
    }

    //목표 리스트 조회(목표 생성시 -> 세부 내용 조회X) 카테고리별 친구 목표
    @Transactional(readOnly = true)
    public List<GoalResponseDto.GoalCreateListDto> getFriendGoalsByCategory(Long userId, GoalCategory goalCategory) {
        userService.getUserbyUserId(userId);

        List<UserGoal> friendGoals = userGoalRepository.findFriendGoalsByCategory(userId, goalCategory);

        return friendGoals.stream()
                .map(userGoal -> {
                    User creator = userGoalRepository.findByGoalIdAndStatus(
                            userGoal.getGoal().getId(), Status.ADMIN).getUser();
                    int currentParticipants = userGoalRepository.countByGoalId(userGoal.getGoal().getId());
                    int remainingSlots = userGoal.getGoal().getLimitFriendCount() - currentParticipants;
                    return GoalConvertor.toGoalCreateListDto(userGoal, creator, remainingSlots);
                })
                .collect(Collectors.toList());
    }

    //목표 리스트 조회(목표 생성시 -> 세부 내용 조회X) 카테고리별 커뮤니티 목표
    @Transactional(readOnly = true)
    public List<GoalResponseDto.GoalCreateListDto> getCommunityGoalsByCategory(GoalCategory goalCategory) {
        List<UserGoal> communityGoals = userGoalRepository.findCommunityGoalsByCategory(goalCategory);

        return communityGoals.stream()
                .map(userGoal -> {
                    User creator = userGoalRepository.findByGoalIdAndStatus(
                            userGoal.getGoal().getId(), Status.ADMIN).getUser();
                    int currentParticipants = userGoalRepository.countByGoalId(userGoal.getGoal().getId());
                    int remainingSlots = userGoal.getGoal().getLimitFriendCount() - currentParticipants;
                    return GoalConvertor.toGoalCreateListDto(userGoal, creator, remainingSlots);
                })
                .collect(Collectors.toList());
    }

    //내 목표 조회(리스트)
    @Transactional(readOnly = true)
    public List<GoalResponseDto.MyGoalListDto> getMyGoals(Long userId) {
        User user = userService.getUserbyUserId(userId);

        List<UserGoal> userGoals = userGoalRepository.findByUserId(userId);

        return userGoals.stream()
                .map(GoalConvertor::toMyGoalListDto)
                .collect(Collectors.toList());
    }

    //친구 목표 조회(리스트)
    @Transactional(readOnly = true)
    public List<GoalResponseDto.FriendGoalListDto> getFriendGoals(Long userId, Long friendsId) {
        User user = userService.getUserbyUserId(userId);
        userService.getUserbyUserId(friendsId);

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

        goalMemoRepository.deleteByGoalId(goalId);

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

        UserGoal myuserGoal = userGoalService.getByGoalIdAndUserId(goalId,userId);

        List<UserGoal> userGoals = userGoalRepository.findByGoalId(goalId);

        return userGoals.stream()
                .map(userGoal -> {
                    User user = userGoal.getUser();
                    String todayTime;

                    if (goal.getVerificationType() == VerificationType.TIMER) {
                        // 타이머인 경우 실제 시간 계산
                        LocalTime totalTime = timerVerificationReadService.getTodayTotalTime(myuserGoal);
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

    //특정일 메모 조회
    @Transactional(readOnly = true)
    public GoalResponseDto.GoalMemoReadDto getMemo(Long userId, Long goalId, LocalDate date) {
        UserGoal userGoal = userGoalService.getByGoalIdAndUserId(goalId, userId);

        Optional<GoalMemo> memoOpt = goalMemoRepository
                .findByUserGoalAndMemoDate(userGoal, date);

        if (memoOpt.isPresent()) {
            GoalMemo memo = memoOpt.get();
            return GoalResponseDto.GoalMemoReadDto.builder()
                    .memo(memo.getMemo())
                    .memoDate(memo.getMemoDate())
                    .exists(true)
                    .build();
        } else {
            return GoalResponseDto.GoalMemoReadDto.builder()
                    .memo("")
                    .memoDate(date)
                    .exists(false)
                    .build();
        }
    }

    //리포트용 특정 기간 메모 조회
    @Transactional(readOnly = true)
    public List<GoalResponseDto.GoalMemoReadDto> getMemosByPeriod(
            Long userId,
            Long goalId,
            LocalDate startDate,
            LocalDate endDate) {

        List<GoalResponseDto.GoalMemoReadDto> memoList = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            GoalResponseDto.GoalMemoReadDto memo = getMemo(userId, goalId, currentDate);
            memoList.add(memo);
            currentDate = currentDate.plusDays(1);
        }

        return memoList;
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

    public GoalResponseDto.DailyVerifiedGoalsResponse getDailyVerifiedGoals(Long userId, LocalDate date) {
        List<UserGoal> userGoals = userGoalRepository.findByUserId(userId);
        List<Goal> verifiedGoals = new ArrayList<>();

        for (UserGoal userGoal : userGoals) {
            Goal goal = userGoal.getGoal();
            boolean hasVerification = false;

            if (goal.getVerificationType() == VerificationType.TIMER) {
                hasVerification = timerVerificationRepository.existsByUserGoalAndDate(userGoal.getId(), date);
            } else if (goal.getVerificationType() == VerificationType.PHOTO) {
                hasVerification = photoVerificationRepository.existsByUserGoalAndDate(userGoal.getId(), date);
            }
            if (hasVerification) {
                verifiedGoals.add(goal);
            }
        }

        return GoalConvertor.toDailyVerifiedGoalsResponse(date, verifiedGoals);
    }

    //헬퍼 메서드
    public User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_USER));
    }

    @Override
    public Goal getGoalById(Long id) {
        return goalRepository.findById(id).orElseThrow(() -> new ChallengeException(ErrorStatus.NOT_FOUND_CHALLENGE));
    }

    private void validateGoalCreationLimit(User user) {
        if (user.getUserLevel() == UserLevel.LEVEL_MAX) {
            return;
        }

        int currentActiveGoals = userGoalRepository.countByUserIdAndIsActiveTrue(user.getId());
        int maxGoalCount = user.getUserLevel().getValue();

        if (currentActiveGoals > maxGoalCount) {
            String levelUpGuide = getLevelUpGuide(user.getUserLevel());
                throw new GoalException(ErrorStatus.GOAL_CREATION_LIMIT_EXCEEDED)
                        .setCustomMessage(String.format(
                                "현재 레벨 %d에서는 최대 %d개의 목표만 생성할 수 있습니다.%s",
                                user.getUserLevel().getValue(), maxGoalCount, levelUpGuide));
        }
    }

    @Override
    @Transactional
    public Goal findGoalById(Long goalId) {
        return goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("목표를 찾을 수 없습니다."));
    }

    private void validateEndDate(Date endDate) {
        LocalDate endLocalDate = endDate.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
        if (endLocalDate.isBefore(LocalDate.now())) {
            throw new GoalException(ErrorStatus.INVALID_GOAL_END_DATE);
        }
    }

    private void validateGoalName(String goalName, Long userId) {
        if (goalName == null || goalName.trim().isEmpty()) {
            throw new GoalException(ErrorStatus.NOT_FOUND_GOAL_TITLE);
        }
    }

    private String getLevelUpGuide(UserLevel currentLevel) {
        switch (currentLevel) {
            case LEVEL_1:
                return "다음 단계: 첫 번째 목표를 7일 내에 50% 이상 달성하면 목표 2개까지 생성할 수 있어요!";

            case LEVEL_2:
                return "다음 단계: 새로운 목표를 추가하고 매일 1회씩 7일간 기록하면 목표 3개까지 생성할 수 있어요!";

            case LEVEL_3:
                return "다음 단계: 2개의 활성 목표를 모두 7일 내에 50% 이상 달성하면 목표 4개까지 생성할 수 있어요!";

            case LEVEL_4:
                return "다음 단계: 7일간 2개 이상의 활성화 목표 달성률을 50% 이상으로 유지하면 목표 5개까지 생성할 수 있어요!";

            case LEVEL_5:
                return "다음 단계: 새 목표를 추가하고 14일간 3개 이상의 활성화 목표 달성률을 50% 이상으로 유지하면 목표 6개까지 생성할 수 있어요!";

            case LEVEL_6:
                return "다음 단계: 7일간 전체 활성화 목표 달성률을 50% 이상으로 유지하면 목표 7개까지 생성할 수 있어요!";

            case LEVEL_7:
                return "다음 단계: 14일간 2개 이상의 활성화 목표 달성률을 50% 이상으로 유지하면 목표 8개까지 생성할 수 있어요!";

            case LEVEL_8:
                return "다음 단계: 14일간 전체 활성화 목표 달성률을 50% 이상으로 유지하면 목표 9개까지 생성할 수 있어요!";

            case LEVEL_9:
                return "다음 단계: 새 목표를 추가하고 14일간 3개 이상의 활성화 목표 달성률을 50% 이상으로 유지하면 목표 10개까지 생성할 수 있어요!";

            case LEVEL_10:
                return "프리미엄 구독으로 무제한 목표 생성이 가능해요!";

            default:
                return "목표를 꾸준히 달성하여 레벨을 올려보세요!";
        }
    }
}
