package com.planup.planup.domain.friend.service;

import com.planup.planup.domain.friend.converter.FriendConverter;
import com.planup.planup.domain.friend.dto.FriendResponseDTO;
import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.friend.entity.FriendStatus;
import com.planup.planup.domain.friend.repository.FriendRepository;
import com.planup.planup.domain.notification.service.NotificationService;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.UserService;
import com.planup.planup.domain.verification.repository.PhotoVerificationRepository;
import com.planup.planup.domain.verification.service.TimerVerificationReadService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FriendReadServiceImpl implements FriendReadService {

    private final FriendRepository friendRepository;

    private final UserService userService;
    private final FriendConverter friendConverter;
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

        int goalCnt = friend.getUserGoals() != null ? friend.getUserGoals().size() : 0;
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
}
