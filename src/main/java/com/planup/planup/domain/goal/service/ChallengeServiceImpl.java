package com.planup.planup.domain.goal.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.ChallengeException;
import com.planup.planup.domain.goal.convertor.ChallengeConverter;
import com.planup.planup.domain.goal.dto.ChallengeRequestDTO;
import com.planup.planup.domain.goal.dto.ChallengeResponseDTO;
import com.planup.planup.domain.goal.entity.Challenge;
import com.planup.planup.domain.goal.entity.Enum.ChallengeStatus;
import com.planup.planup.domain.goal.entity.Enum.GoalType;
import com.planup.planup.domain.goal.entity.Enum.Status;
import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.PhotoChallenge;
import com.planup.planup.domain.goal.entity.TimeChallenge;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goal.repository.PhotoChallengeRepository;
import com.planup.planup.domain.goal.repository.TimeChallengeRepository;
import com.planup.planup.domain.goal.repository.UserGoalRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallengeServiceImpl implements ChallengeService{

    private final TimeChallengeRepository timeChallengeRepository;
    private final PhotoChallengeRepository photoChallengeRepository;
    private final GoalService goalService;
    private final UserService userService;
    private final UserGoalRepository userGoalRepository;
    private final UserGoalService userGoalService;

    @Override
    public Challenge createChallenge(Long userId, ChallengeRequestDTO.create dto) {

        User user = userService.getUserbyUserId(userId);
        List<User> friendList = dto.friendIdList().stream().map(userService::getUserbyUserId).toList();

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
            createUserGoal(user, friendList, timeChallenge, VerificationType.TIMER);

            return timeChallengeRepository.save(timeChallenge);
        }

        //Goal 케이스
        if (dto.goalType() == GoalType.CHALLENGE_PHOTO) {
            if (dto.photoChallenge() == null) {
                throw new ChallengeException(ErrorStatus.MISSING_PHOTO_CHALLENGE_INFO);
            }

            PhotoChallenge photoChallenge = ChallengeConverter.toPhotoChallenge(dto);
            createUserGoal(user, friendList, photoChallenge, VerificationType.PHOTO);

            return photoChallengeRepository.save(photoChallenge);
        }

        throw new ChallengeException(ErrorStatus.INVALID_CHALLENGE_TYPE);
    }

    private void createUserGoal(User user, List<User> friends, Goal timeChallenge, VerificationType type) {
        //TODO: 별도의 서비스 로직으로 이전
        UserGoal userGoalAdmin = UserGoal.builder()
                .user(user)
                .isActive(false)
                .verificationType(type)
                .status(Status.ADMIN)
                .goal(timeChallenge)
                .build();

        userGoalRepository.save(userGoalAdmin);

        for (User friend : friends) {
            UserGoal userGoalMember = UserGoal.builder()
                    .user(friend)
                    .isActive(false)
                    .verificationType(type)
                    .status(Status.MEMBER)
                    .goal(timeChallenge)
                    .build();

            userGoalRepository.save(userGoalMember);
        }
    }

    public ChallengeResponseDTO.ChallengeResponseInfo getChallengeInfo(Long challengeId) {
        Goal goal = goalService.getGoalById(challengeId);

        if (goal.getGoalType() == GoalType.CHALLENGE_PHOTO) {
            if (goal instanceof PhotoChallenge) {
                PhotoChallenge photoChallenge = (PhotoChallenge) goal;
                ChallengeResponseDTO.ChallengeResponseInfo challengeResponseInfo = ChallengeConverter.toChallengeResponseInfoPhotoVer(photoChallenge);
                return challengeResponseInfo;

            }
        } else if (goal.getGoalType() == GoalType.CHALLENGE_TIME) {
            if (goal instanceof TimeChallenge) {
                TimeChallenge timeChallenge = (TimeChallenge) goal;
                ChallengeResponseDTO.ChallengeResponseInfo challengeResponseInfo = ChallengeConverter.toChallengeResponseInfoTimeVer(timeChallenge);
                return challengeResponseInfo;
            }
        }

        throw new ChallengeException(ErrorStatus.NOT_FOUND_CHALLENGE);
    }

    //Goal Repo에 가서 challange를 찾아온다
    public Challenge getChallengeById(Long challengeId) {
        Goal goal = goalService.getGoalById(challengeId);

        if (goal instanceof PhotoChallenge || goal instanceof TimeChallenge) {
            return (Challenge) goal;
        } else throw new ChallengeException(ErrorStatus.NOT_FOUND_CHALLENGE);
    }

    //챌린지 요청을 거절한다
    @Override
    public void rejectChallengeRequest(Long userId, Long challengeId) {
        User user = userService.getUserbyUserId(userId);
        Goal goal = goalService.getGoalById(challengeId);
        UserGoal userGoal = userGoalService.getUserGoalByUserAndGoal(user, goal);
        Challenge challenge = getChallengeById(challengeId);

        challenge.setChallengeStatus(ChallengeStatus.REJECTED);
    }

    //챌린지 요청을 수락한다.
    @Override
    public void acceptChallengeRequest(Long userId, Long challengeId) {
        User user = userService.getUserbyUserId(userId);
        Goal goal = goalService.getGoalById(challengeId);
        UserGoal userGoal = userGoalService.getUserGoalByUserAndGoal(user, goal);
        Challenge challenge = getChallengeById(challengeId);

        challenge.setChallengeStatus(ChallengeStatus.ACCEPTED);
        userGoal.setActive(true, user);
    }
}
