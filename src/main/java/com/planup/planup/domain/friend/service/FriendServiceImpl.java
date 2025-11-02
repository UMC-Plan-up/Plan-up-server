package com.planup.planup.domain.friend.service;

import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.friend.entity.FriendStatus;
import com.planup.planup.domain.friend.dto.FriendResponseDTO;
import com.planup.planup.domain.friend.dto.BlockedFriendResponseDTO;
import com.planup.planup.domain.friend.repository.FriendRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.UserService;
import com.planup.planup.domain.verification.repository.PhotoVerificationRepository;
import com.planup.planup.domain.verification.service.TimerVerificationReadService;
import com.planup.planup.domain.notification.service.NotificationService;
import com.planup.planup.domain.notification.entity.NotificationType;
import com.planup.planup.domain.notification.entity.TargetType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.planup.planup.domain.friend.converter.FriendConverter;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.UserException;
import java.util.Collections;

@Service
@AllArgsConstructor
@Slf4j
public class FriendServiceImpl implements FriendService {

    private final FriendRepository friendRepository;
    private final UserService userService;
    private final FriendConverter friendConverter;
    private final TimerVerificationReadService timerVerificationService;
    private final PhotoVerificationRepository photoVerificationRepository;
    private final NotificationService notificationService;  

    @Override
    @Transactional(readOnly = true)
    public List<FriendResponseDTO.FriendSummaryList> getFriendSummeryList(Long userId) {

        User user = userService.getUserbyUserId(userId);

        List<Friend> friendList = friendRepository.findByStatusAndUserIdOrStatusAndFriendIdOrderByCreatedAtDesc(FriendStatus.ACCEPTED, user.getId(), FriendStatus.ACCEPTED, user.getId());

//        //Friend 객체의 friend, user 중 내가 아닌 친구를 뽑는다.
        List<User> list = friendList.stream()
                .map(f -> f.getFriend().equals(user) ? f.getUser() : f.getFriend())
                .distinct().toList();

        return List.of(friendConverter.toFriendSummaryList(list.stream()
                .map(this::getFriendInfoSummary)
                .toList()));
    }

    @Override
    @Transactional
    public boolean deleteFriend(Long userId, Long friendId) {
        // 1. userId와 friendId로 Friend 엔티티를 찾는다.
        // 2. 해당 Friend 엔티티를 삭제한다.
        // 3. 성공적으로 삭제했으면 true, 아니면 false 반환

        List<Friend> friends = friendRepository.findByStatusAndUserIdOrStatusAndFriendIdOrderByCreatedAtDesc(
                FriendStatus.ACCEPTED, userId, FriendStatus.ACCEPTED, userId);

        Friend friend = friends.stream()
                .filter(f -> (f.getUser().getId().equals(friendId) || f.getFriend().getId().equals(friendId)))
                .findFirst()
                .orElse(null);

        if (friend != null) {
            friendRepository.delete(friend);
            return true;
        }
        // 친구를 찾지 못했을 때
        throw new UserException(ErrorStatus._BAD_REQUEST);
    }

    @Override
    @Transactional
    public boolean blockFriend(Long userId, Long friendId) {
        // 1. userId와 friendId로 Friend 엔티티를 찾는다.
        List<Friend> friends = friendRepository.findByStatusAndUserIdOrStatusAndFriendIdOrderByCreatedAtDesc(
                FriendStatus.ACCEPTED, userId, FriendStatus.ACCEPTED, userId);

        Friend friend = friends.stream()
                .filter(f -> (f.getUser().getId().equals(friendId) || f.getFriend().getId().equals(friendId)))
                .findFirst()
                .orElse(null);

        if (friend != null) {
            friend.setStatus(FriendStatus.BLOCKED); // 상태 변경
            friendRepository.save(friend);          // 저장
            return true;
        }
        // 친구를 찾지 못했을 때
        throw new UserException(ErrorStatus._BAD_REQUEST);
    }

    @Override
    @Transactional
    public boolean reportFriend(Long userId, Long friendId, String reason, boolean block) {
        // 1. Friend 엔티티 찾기 (ACCEPTED 또는 BLOCKED 상태의 친구 관계)
        List<Friend> acceptedFriends = friendRepository.findByStatusAndUserIdOrStatusAndFriendIdOrderByCreatedAtDesc(
                FriendStatus.ACCEPTED, userId, FriendStatus.ACCEPTED, userId);

        List<Friend> blockedFriends = friendRepository.findByStatusAndUserIdOrStatusAndFriendIdOrderByCreatedAtDesc(
                FriendStatus.BLOCKED, userId, FriendStatus.BLOCKED, userId);

        // 두 리스트 합치기
        List<Friend> allFriends = new ArrayList<>();
        allFriends.addAll(acceptedFriends);
        allFriends.addAll(blockedFriends);

        Friend friend = allFriends.stream()
                .filter(f -> (f.getUser().getId().equals(friendId) || f.getFriend().getId().equals(friendId)))
                .findFirst()
                .orElse(null);

        if (friend != null) {
            // 신고 사유 저장 (별도 테이블이 있다면 Report 엔티티에 저장, 없다면 로그 등)
            System.out.println("신고 사유: " + reason);

            // 차단 여부에 따라 상태 변경 (이미 차단된 상태라면 상태 변경하지 않음)
            if (block && friend.getStatus() != FriendStatus.BLOCKED) {
                friend.setStatus(FriendStatus.BLOCKED);
                friendRepository.save(friend);
            }
            return true;
        }
        // 친구를 찾지 못했을 때
        throw new UserException(ErrorStatus._BAD_REQUEST);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendResponseDTO.FriendInfoSummary> getRequestedFriends(Long userId) {
        List<Friend> friendRequests = friendRepository.findByStatusAndFriend_IdOrderByCreatedAtDesc(
                FriendStatus.REQUESTED, userId);

        // 신청한 친구(User) 정보 추출
        // 친구 신청이 없을 때 빈 리스트 반환
        if (friendRequests.isEmpty()) {
            return Collections.emptyList();
        }

        return friendRequests.stream()
                .map(Friend::getUser)
                .map(this::getFriendInfoSummary)
                .collect(Collectors.toList());
    }

    private FriendResponseDTO.FriendInfoSummary getFriendInfoSummary(User friend) {
//        User friend = req.getUser();

        int goalCnt = friend.getUserGoals() != null ? friend.getUserGoals().size() : 0;
        boolean isNewPhotoVerify = checkTodayPhotoVerification(friend); // 서비스 내 유틸/레포 호출
        LocalTime todayTime = calculateTodayTotalTime(friend);          // 기존 메서드 유지

        return friendConverter.toFriendSummary(friend, goalCnt, isNewPhotoVerify, todayTime, friend.getProfileImg());
    }

    private LocalTime calculateTodayTotalTime(User user) {
        try {
            // 사용자의 모든 목표에 대해 오늘 타이머 시간을 합계
            long totalSeconds = user.getUserGoals().stream()
                    .mapToLong(userGoal -> {
                        try {
                            LocalTime goalTime = timerVerificationService.getTodayTotalTime(userGoal);
                            return goalTime.toSecondOfDay();
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
    @Transactional
    public boolean rejectFriendRequest(Long userId, Long friendId) {
        // 나(userId)에게 친구 신청한 친구(friendId)를 찾음
        List<Friend> requests = friendRepository.findByStatusAndFriend_IdOrderByCreatedAtDesc(
                FriendStatus.REQUESTED, userId);

        Friend friend = requests.stream()
                .filter(f -> f.getUser().getId().equals(friendId))
                .findFirst()
                .orElse(null);

        if (friend != null) {
            friendRepository.delete(friend); // 엔티티 삭제
            
            // 친구 신청 거절 알림 생성
            notificationService.createNotification(
                friendId,           // receiverId (친구 신청을 보낸 사람)
                userId,             // senderId (친구 신청을 거절한 사람)
                NotificationType.FRIEND_REQUEST_REJECTED,
                TargetType.USER,
                userId              // targetId (친구 신청을 거절한 사람의 ID)
            );
            
            return true;
        }
        // 친구 신청을 찾지 못했을 때
        throw new UserException(ErrorStatus._BAD_REQUEST);
    }

    @Override
    @Transactional
    public boolean acceptFriendRequest(Long userId, Long friendId) {
        // 나(userId)에게 친구 신청한 친구(friendId)를 찾음
        List<Friend> requests = friendRepository.findByStatusAndFriend_IdOrderByCreatedAtDesc(
                FriendStatus.REQUESTED, userId);

        Friend friend = requests.stream()
                .filter(f -> f.getUser().getId().equals(friendId))
                .findFirst()
                .orElse(null);

        if (friend != null) {
            friend.setStatus(FriendStatus.ACCEPTED); // 상태를 ACCEPTED로 변경
            friendRepository.save(friend);
            
            // 친구 신청 수락 알림 생성 - 양쪽 모두에게
            // 1. 친구 신청을 보낸 사람에게 알림
            notificationService.createNotification(
                friendId,           // receiverId (친구 신청을 보낸 사람)
                userId,             // senderId (친구 신청을 수락한 사람)
                NotificationType.FRIEND_REQUEST_ACCEPTED,
                TargetType.USER,
                userId              // targetId (친구 신청을 수락한 사람의 ID)
            );
            
            // 2. 친구 신청을 수락한 사람에게도 알림
            notificationService.createNotification(
                userId,             // receiverId (친구 신청을 수락한 사람)
                friendId,           // senderId (친구 신청을 보낸 사람)
                NotificationType.FRIEND_REQUEST_ACCEPTED,
                TargetType.USER,
                friendId            // targetId (친구 신청을 보낸 사람의 ID)
            );
            
            return true;
        }
        // 친구 신청을 찾지 못했을 때
        throw new UserException(ErrorStatus._BAD_REQUEST);
    }

    @Override
    @Transactional
    public boolean sendFriendRequest(Long userId, Long friendId) {
        // 이미 친구 관계가 있는지, 이미 신청했는지 체크(중복 방지)
        List<Friend> existingAccepted = friendRepository.findByStatusAndUserIdOrStatusAndFriendIdOrderByCreatedAtDesc(
                FriendStatus.ACCEPTED, userId, FriendStatus.ACCEPTED, userId);

        List<Friend> existingRequested = friendRepository.findByStatusAndUserIdOrStatusAndFriendIdOrderByCreatedAtDesc(
                FriendStatus.REQUESTED, userId, FriendStatus.REQUESTED, userId);

        // 두 리스트 합치기
        List<Friend> allExisting = new ArrayList<>();
        allExisting.addAll(existingAccepted);
        allExisting.addAll(existingRequested);

        boolean alreadyRequested = allExisting.stream()
                .anyMatch(f -> (f.getUser().getId().equals(friendId) || f.getFriend().getId().equals(friendId)));

        if (alreadyRequested) {
            return false; // 이미 친구거나 신청함
        }

        // 유저 엔티티 조회
        User user = userService.getUserbyUserId(userId);
        User friendUser = userService.getUserbyUserId(friendId);

        // Friend 엔티티 생성
        Friend friendRequest = new Friend();
        friendRequest.setUser(user);
        friendRequest.setFriend(friendUser);
        friendRequest.setStatus(FriendStatus.REQUESTED);

        //TODO: 실제 서비스에서 제거
        if (userId.equals("dummy11@planup.com")) friendRequest.setStatus(FriendStatus.ACCEPTED);

        friendRepository.save(friendRequest);
        
        // 친구 신청 알림 생성
        notificationService.createNotification(
            friendId,           // receiverId (친구 신청 받는 사람)
            userId,             // senderId (친구 신청 보내는 사람)
            NotificationType.FRIEND_REQUEST_SENT,
            TargetType.USER,
            userId              // targetId (친구 신청 보낸 사람의 ID)
        );
        
        return true;
    }

    //챌린지에서 사용할 friend dto를 만들어 반환한다.
    @Override
    @Transactional(readOnly = true)
    public List<FriendResponseDTO.FriendInfoInChallengeCreate> getFrinedListInChallenge(Long userId) {
        List<Friend> friendList = friendRepository.findByStatusAndUserIdOrStatusAndFriendIdOrderByCreatedAtDesc(FriendStatus.ACCEPTED, userId, FriendStatus.ACCEPTED, userId);
        List<FriendResponseDTO.FriendInfoInChallengeCreate> dtoList = friendList.stream()
                .map(friend -> friendConverter.toFriendInfoChallenge(friend.getFriend())) // 또는 getUser()
                .distinct()
                .collect(Collectors.toList());
        return dtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlockedFriendResponseDTO> getBlockedFriends(Long userId) {
        User user = userService.getUserbyUserId(userId);

        // 사용자가 차단한 친구 목록 조회 (user가 차단한 경우만)
        List<Friend> blockedFriends = friendRepository.findByUserAndStatusOrderByCreatedAtDesc(user, FriendStatus.BLOCKED);

        return blockedFriends.stream()
                .map(friend -> {
                    // user가 차단한 대상은 friend 필드에 있음
                    User blockedUser = friend.getFriend();
                    return BlockedFriendResponseDTO.builder()
                            .friendId(blockedUser.getId())
                            .friendNickname(blockedUser.getNickname())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Long unblockFriend(Long userId, String friendNickname) {
        User user = userService.getUserbyUserId(userId);

        // 사용자가 해당 닉네임의 친구를 차단한 관계를 찾음
        Optional<Friend> blockedFriend = friendRepository.findByUserAndFriend_NicknameAndStatus(user, friendNickname, FriendStatus.BLOCKED);

        if (blockedFriend.isPresent()) {
            // 차단 관계를 삭제
            friendRepository.delete(blockedFriend.get());
            return blockedFriend.get().getFriendId(userId);
        }

        // 차단 관계를 찾지 못했을 때
        throw new UserException(ErrorStatus._BAD_REQUEST);
    }

    @Override
    @Transactional(readOnly = true)
    public void isFriend(Long userId, Long creatorId) {
        List<Friend> friendRelations = friendRepository
                .findByStatusAndUserIdOrStatusAndFriendIdOrderByCreatedAtDesc(
                        FriendStatus.ACCEPTED, userId,
                        FriendStatus.ACCEPTED, userId);

        boolean isFriend = friendRelations.stream()
                .anyMatch(friend ->
                        (friend.getUser().getId().equals(creatorId) && friend.getFriend().getId().equals(userId)) ||
                                (friend.getFriend().getId().equals(creatorId) && friend.getUser().getId().equals(userId))
                );
        if (!isFriend) {
            throw new RuntimeException("친구 관계가 아니므로 목표를 조회할 수 없습니다.");
        }
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
