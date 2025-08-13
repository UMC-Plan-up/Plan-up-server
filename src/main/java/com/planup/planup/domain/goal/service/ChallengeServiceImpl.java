package com.planup.planup.domain.goal.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.ChallengeException;
import com.planup.planup.apiPayload.exception.custom.UserGoalException;
import com.planup.planup.domain.global.service.AchievementCalculationService;
import com.planup.planup.domain.goal.convertor.ChallengeConverter;
import com.planup.planup.domain.goal.dto.ChallengeRequestDTO;
import com.planup.planup.domain.goal.dto.ChallengeResponseDTO;
import com.planup.planup.domain.goal.entity.Challenge;
import com.planup.planup.domain.goal.entity.Enum.ChallengeStatus;
import com.planup.planup.domain.goal.entity.Enum.GoalType;
import com.planup.planup.domain.goal.entity.Enum.Status;
import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.TimeChallenge;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goal.repository.ChallengeRepository;
import com.planup.planup.domain.goal.repository.TimeChallengeRepository;
import com.planup.planup.domain.goal.repository.UserGoalRepository;
import com.planup.planup.domain.notification.service.NotificationService;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.UserService;
import com.planup.planup.domain.verification.service.PhotoVerificationReadService;
import com.planup.planup.domain.verification.service.PhotoVerificationService;
import com.planup.planup.domain.verification.service.TimerVerificationReadService;
import com.planup.planup.domain.verification.service.TimerVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallengeServiceImpl implements ChallengeService{

    private final TimeChallengeRepository timeChallengeRepository;
    private final ChallengeRepository challengeRepository;
    private final GoalService goalService;
    private final UserService userService;
    private final UserGoalRepository userGoalRepository;
    private final UserGoalService userGoalService;
    private final PhotoVerificationService photoVerificationService;
    private final TimerVerificationService timerVerificationService;
    private final AchievementCalculationService achievementCalculationService;
    private final NotificationService notificationService;
    private final PhotoVerificationReadService photoVerificationReadService;
    private final TimerVerificationReadService timerVerificationReadService;

    @Override
    @Transactional
    public Challenge createChallenge(Long userId, ChallengeRequestDTO.create dto) {

        User user = userService.getUserbyUserId(userId);
        User friend = userService.getUserbyUserId(dto.friendId());

        //challenge type이 아닌 경우 예외 처리
        if (dto.goalType() == GoalType.FRIEND || dto.goalType() == GoalType.COMMUNITY) {
            throw new ChallengeException(ErrorStatus.INVALID_HTTP_CHALLENGE_METHOD);
        }

        //Time 케이스
        if (dto.goalType() == GoalType.CHALLENGE_TIME) {
            if (dto.timeChallenge() == null) {
                throw new ChallengeException(ErrorStatus.MISSING_TIME_CHALLENGE_INFO);
            }

            TimeChallenge timeChallenge = ChallengeConverter.toTimeChallenge(dto);
            createUserGoal(user, friend, timeChallenge, VerificationType.TIMER);

            return timeChallengeRepository.save(timeChallenge);
        }

        //photo 케이스
        if (dto.goalType() == GoalType.CHALLENGE_PHOTO) {

            Challenge photoChallenge = ChallengeConverter.toPhotoChallenge(dto);
            createUserGoal(user, friend, photoChallenge, VerificationType.PHOTO);

            return challengeRepository.save(photoChallenge);
        }

        throw new ChallengeException(ErrorStatus.INVALID_CHALLENGE_TYPE);
    }

    private void createUserGoal(User user, User friend, Goal timeChallenge, VerificationType type) {
        //TODO: 별도의 서비스 로직으로 이전
        createPerUserGoal(user, type, Status.ADMIN, timeChallenge);

        createPerUserGoal(friend, type, Status.MEMBER, timeChallenge);
    }

    private void createPerUserGoal(User friend, VerificationType type, Status member, Goal timeChallenge) {
        UserGoal userGoalMember = UserGoal.builder()
                .user(friend)
                .isActive(false)
                .status(member)
                .goal(timeChallenge)
                .build();

        userGoalRepository.save(userGoalMember);
    }

    @Transactional(readOnly = true)
    public ChallengeResponseDTO.ChallengeResponseInfo getChallengeInfo(Long challengeId) {
        Goal goal = goalService.getGoalById(challengeId);

        List<UserGoal> userGoalList = userGoalService.getUserGoalListByGoal(goal);

        UserGoal first = userGoalList.stream().filter(userGoal ->
                userGoal.getStatus().equals(Status.ADMIN)
        ).findFirst().orElseThrow(() -> new UserGoalException(ErrorStatus.NOT_FOUND_CHALLENGE));

        String nickname = first.getUser().getNickname();

        if (goal.getGoalType() == GoalType.CHALLENGE_PHOTO) {
            if (goal instanceof Challenge photoChallenge) {
                return ChallengeConverter.toChallengeResponseInfoPhotoVer(photoChallenge, nickname);

            }
        } else if (goal.getGoalType() == GoalType.CHALLENGE_TIME) {
            if (goal instanceof TimeChallenge timeChallenge) {
                return ChallengeConverter.toChallengeResponseInfoTimeVer(timeChallenge);
            }
        }

        throw new ChallengeException(ErrorStatus.NOT_FOUND_CHALLENGE);
    }

    //Goal Repo에 가서 challange를 찾아온다
    @Transactional(readOnly = true)
    public Challenge getChallengeById(Long challengeId) {
        Goal goal = goalService.getGoalById(challengeId);

        if (goal instanceof Challenge) {
            return (Challenge) goal;
        } else throw new ChallengeException(ErrorStatus.NOT_FOUND_CHALLENGE);
    }

    //챌린지 요청을 거절한다
    @Override
    @Transactional
    public void rejectChallengeRequest(Long userId, Long challengeId) {
        User user = userService.getUserbyUserId(userId);
        Goal goal = goalService.getGoalById(challengeId);
        Challenge challenge = getChallengeById(challengeId);

        challenge.setChallengeStatus(ChallengeStatus.REJECTED);
    }

    //챌린지 요청을 수락한다.
    @Override
    @Transactional
    public void acceptChallengeRequest(Long userId, Long challengeId) {
        User user = userService.getUserbyUserId(userId);
        Goal goal = goalService.getGoalById(challengeId);
        UserGoal userGoal = userGoalService.getUserGoalByUserAndGoal(user, goal);
        Challenge challenge = getChallengeById(challengeId);

        challenge.setChallengeStatus(ChallengeStatus.ACCEPTED);
        userGoal.setActive(true, user);
    }

    //챌린지에 대한 새로운 패널티 제안
    @Override
    @Transactional
    public void reRequestPenalty(Long userId, ChallengeRequestDTO.ReRequestPenalty dto) {
        User user = userService.getUserbyUserId(userId);

        Challenge challenge = getChallengeById(dto.id());

        //기존에 제안받은 사람 아니면 예외처리
        if (!isChallengeMember(user, challenge)) {
            throw new ChallengeException(ErrorStatus._NOT_ALLOWED);
        }

        challenge.setPenalty(dto.penalty());

        //새롭게 챌린지에 추가한 유저에 대해 usergoal을 추가한다. (기존에 없어야 한다.)
        addChallengeMember(dto.friendIdList(), challenge);
    }

    @Override
    @Transactional(readOnly = true)
    public String getChallengeName(Long userId, Long challengeId) {
        Challenge challengeById = getChallengeById(challengeId);
        return challengeById.getGoalName();
    }

    private boolean isChallengeMember(User user, Challenge challenge) {
        List<UserGoal> userGoalList = userGoalService.getUserGoalListByGoal(challenge);
        for (UserGoal userGoal : userGoalList) {
            if (userGoal.getUser().equals(user)) return true;
        }
        return false;
    }

    private void addChallengeMember(List<Long> friendList, Challenge challenge) {
        List<UserGoal> userGoalList = userGoalService.getUserGoalListByGoal(challenge);
        List<User> users = userGoalList.stream().map(UserGoal::getUser).toList();

        Set<Long> existingUserIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        VerificationType type = challenge.getGoalType().equals(GoalType.CHALLENGE_PHOTO)
                ? VerificationType.PHOTO
                : VerificationType.TIMER;

        for (Long friendId : friendList) {
            if (existingUserIds.contains(friendId)) {
                continue; // 이미 존재하는 유저면 건너뜀
            }
            User user = userService.getUserbyUserId(friendId);
            createPerUserGoal(user, type, Status.MEMBER, challenge);
        }
    }

    @Override
    public ChallengeResponseDTO.ChallengeResultResponseDTO getChallengeResult(User user, Long challengeId) {
        Challenge challenge = getChallengeById(challengeId);

        UserGoal myUserGoal = getUserGoalByUserAndChallenge(user, challenge);

        UserGoal friendUserGoal = getFriendUserGoalByUserAndChallenge(user, challenge);

        int myPercent = calcAchievementRate(challenge, myUserGoal);
        int friendPercent = calcAchievementRate(challenge, friendUserGoal);

        return ChallengeResponseDTO.ChallengeResultResponseDTO.builder()
                .id(challengeId)
                .myName(user.getNickname())
                .myProfile(user.getProfileImg())
                .friendName(friendUserGoal.getUser().getNickname())
                .friendProfile(friendUserGoal.getUser().getProfileImg())
                .myPercent(myPercent)
                .friendPercent(friendPercent)
                .penalty(challenge.getPenalty())
                .build();
    }

    private static UserGoal getFriendUserGoalByUserAndChallenge(User user, Challenge challenge) {
        return challenge.getUserGoals().stream().filter(u -> !u.getUser().getId().equals(user.getId()))
                .findFirst().orElseThrow(() -> new ChallengeException(ErrorStatus.NOT_FOUND_CHALLENGE));
    }

    private static UserGoal getUserGoalByUserAndChallenge(User user, Challenge challenge) {
        return challenge.getUserGoals().stream().filter(u -> u.getUser().getId().equals(user.getId()))
                .findFirst().orElseThrow(() -> new ChallengeException(ErrorStatus.NOT_FOUND_CHALLENGE));
    }

    private int calcAchievementRate(Challenge challenge, UserGoal myUserGoal) {
        Map<LocalDate, Integer> caledVerification = getCalcVerification(challenge, myUserGoal);
        Map<LocalDate, Integer> localDateIntegerMap = achievementCalculationService.calcAchievementByDay(caledVerification, challenge.getOneDose());
        int sum = localDateIntegerMap.values().stream().filter(Objects::nonNull).mapToInt(Integer::intValue).sum();

        int targetTotal = challenge.getFrequency() * 100;

        return (sum / targetTotal) * 100;
    }

    private Map<LocalDate, Integer> getCalcVerification(Challenge challenge, UserGoal myUserGoal) {
        Map<LocalDate, Integer> verifications = null;
        if (challenge.getVerificationType().equals(VerificationType.TIMER)) {
            verifications = timerVerificationReadService.calculateVerificationWithGoal(myUserGoal);
        } else if (challenge.getVerificationType().equals(VerificationType.PHOTO)) {
            verifications = photoVerificationReadService.calculateVerificationWithGoal(myUserGoal);
        }
        return verifications;
    }

    @Override
    @Transactional
    public void checkChallengeFin(UserGoal userGoal) {
        Challenge challenge = (Challenge) userGoal.getGoal();
        User user = userGoal.getUser();

        //챌린지가 종료되었는지 확인한다.
        int myPercent = calcAchievementRate(challenge, userGoal);
        if (myPercent < 100) {
            return;
        }

        //나와 상대의 userGoal, GOal을 종료로 처리
        challenge.setInActive();

        //관련 알림 전송
        notificationService.createNotification()
    }
}
