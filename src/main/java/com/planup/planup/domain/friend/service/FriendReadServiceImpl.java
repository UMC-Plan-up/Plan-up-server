package com.planup.planup.domain.friend.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.FriendException;
import com.planup.planup.domain.friend.converter.FriendConverter;
import com.planup.planup.domain.friend.dto.FriendResponseDTO;
import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.friend.entity.FriendStatus;
import com.planup.planup.domain.friend.repository.FriendRepository;
import com.planup.planup.domain.goal.service.UserGoalService;
import com.planup.planup.domain.notification.service.NotificationService;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.UserService;
import com.planup.planup.domain.verification.repository.PhotoVerificationRepository;
import com.planup.planup.domain.verification.service.TimerVerificationReadService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@AllArgsConstructor
@Slf4j
public class FriendReadServiceImpl implements FriendReadService {

    private final FriendRepository friendRepository;

    private final UserService userService;
    private final FriendConverter friendConverter;
    private final UserGoalService userGoalService;
    private final TimerVerificationReadService timerVerificationService;
    private final PhotoVerificationRepository photoVerificationRepository;
    private final NotificationService notificationService;

    //친구 리스트를 반환한다.
    public List<FriendResponseDTO.FriendSummaryList> getFriendSummeryList(Long userId) {

        User user = userService.getUserbyUserId(userId);

        List<User> list = getFriendsFromFriendWithoutMe(user);

        return List.of(friendConverter.toFriendSummaryList(list.stream()
                .map(this::getFriendInfoSummary)
                .toList()));
    }

    private List<User> getFriendsFromFriendWithoutMe(User user) {
        List<Friend> friendList = friendRepository.findByStatusAndUserIdOrStatusAndFriendIdOrderByCreatedAtDesc(FriendStatus.ACCEPTED, user.getId(), FriendStatus.ACCEPTED, user.getId());

//      Friend 객체의 friend, user 중 내가 아닌 친구를 뽑는다.
        List<User> list = friendList.stream()
                .map(f -> f.getFriend().equals(user) ? f.getUser() : f.getFriend())
                .distinct().toList();
        return list;
    }

    private FriendResponseDTO.FriendInfoSummary getFriendInfoSummary(User friend) {

        int goalCnt = userGoalService.getUserGoalCount(friend.getId());
        boolean isNewPhotoVerify = checkTodayPhotoVerification(friend); // 서비스 내 유틸/레포 호출
        LocalTime todayTime = calculateTodayTotalTime(friend);          // 기존 메서드 유지

        return friendConverter.toFriendSummary(friend, goalCnt, isNewPhotoVerify, todayTime, friend.getProfileImg());
    }

    private boolean checkTodayPhotoVerification(User user) {
        try {
            return photoVerificationRepository.existsTodayPhotoVerificationByUserId(user.getId());
        } catch (Exception e) {
            log.warn("사용자 {} 오늘 사진 인증 조회 실패: {}", user.getNickname(), e.getMessage());
            return false;
        }
    }

    private LocalTime calculateTodayTotalTime(User user) {
        try {
            // 사용자의 모든 목표에 대해 오늘 타이머 시간을 합계
            long totalSeconds = user.getUserGoals().stream()
                    .mapToLong(userGoal -> {
                        try {
                            return timerVerificationService.getTodayTotalSecTimeByUserGoal(userGoal);
                        } catch (Exception e) {
                            log.warn("사용자 {} 목표 {} 타이머 시간 조회 실패: {}", user.getNickname(), userGoal.getGoal().getId(), e.getMessage());
                            return 0L;
                        }
                    })
                    .sum();

            return LocalTime.ofSecondOfDay(totalSeconds);
        } catch (Exception e) {
            log.warn("사용자 {} 오늘 타이머 시간 계산 실패: {}", user.getNickname(), e.getMessage());
            return LocalTime.of(0, 0, 0);
        }
    }

    @Override
    public List<FriendResponseDTO.FriendInfoSummary> getRequestedFriends(Long userId) {
        List<Friend> friendRequests = friendRepository.findByStatusAndFriendIdOrderByCreatedAtDescWithUser(FriendStatus.REQUESTED, userId);

        //데이터가 없으면 빈 리스트를 만환한다.
        if (friendRequests.isEmpty()) return Collections.emptyList();

        return friendRequests.stream()
                .map(Friend::getUser)
                .map(this::getFriendInfoSummary)
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendResponseDTO.FriendInfoInChallengeCreate> getFrinedListInChallenge(Long userId) {
        List<Friend> friendList = friendRepository.findListByUserIdWithUsers(FriendStatus.ACCEPTED, userId);
        return friendList.stream()
                .map(friend -> friendConverter.toFriendInfoChallenge(friend.getFriendNotMe(userId))) // 또는 getUser()
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public void isFriend(Long userId, Long friendId) {
        Optional<Friend> optionalFriend = friendRepository.findByUserIdAndFriendIdAndStatus(FriendStatus.ACCEPTED, userId, friendId);
        if (optionalFriend.isEmpty()) {
            throw new FriendException(ErrorStatus.NOT_EXIST_USERBLOCK);
        }
    }
}
