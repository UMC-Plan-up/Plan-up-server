package com.planup.planup.domain.friend.service;

import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.friend.entity.FriendStatus;
import com.planup.planup.domain.friend.dto.FriendResponseDTO;
import com.planup.planup.domain.friend.dto.BlockedFriendResponseDTO;
import com.planup.planup.domain.friend.repository.FriendRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.planup.planup.domain.friend.converter.FriendConverter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.UserException;
import java.util.Collections;

@Service
@AllArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final FriendRepository friendRepository;
    private final UserService userService;
    private final FriendConverter friendConverter;

    @Override
    @Transactional(readOnly = true) 
    public List<FriendResponseDTO.FriendSummaryList> getFriendSummeryList(Long userId) {

        User user = userService.getUserbyUserId(userId);

        List<Friend> friendList = friendRepository.findByStatusAndUserIdOrStatusAndFriendIdOrderByCreatedAtDesc(FriendStatus.ACCEPTED, user.getId(), FriendStatus.ACCEPTED, user.getId());

        //Friend 객체의 friend, user 중 내가 아닌 친구를 뽑는다.
        List<User> list = friendList.stream()
                .map(f -> f.getFriend().equals(user) ? f.getUser() : f.getFriend())
                .collect(Collectors.toList());

        return List.of(friendConverter.toFriendSummaryList(list));
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
        List<Friend> friendRequests = friendRepository.findByStatusAndFriendIdOrderByCreatedAtDesc(
            FriendStatus.REQUESTED, userId);

        // 신청한 친구(User) 정보 추출
        // 친구 신청이 없을 때 빈 리스트 반환
        if (friendRequests.isEmpty()) {
            return Collections.emptyList();
        }

        return friendRequests.stream()
                .map(f -> FriendConverter.toFriendSummary(f.getUser()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean rejectFriendRequest(Long userId, Long friendId) {
        // 나(userId)에게 친구 신청한 친구(friendId)를 찾음
        List<Friend> requests = friendRepository.findByStatusAndFriendIdOrderByCreatedAtDesc(
            FriendStatus.REQUESTED, userId);

        Friend friend = requests.stream()
            .filter(f -> f.getUser().getId().equals(friendId))
            .findFirst()
            .orElse(null);

        if (friend != null) {
            friendRepository.delete(friend); // 엔티티 삭제
            return true;
        }
        // 친구 신청을 찾지 못했을 때
        throw new UserException(ErrorStatus._BAD_REQUEST);
    }

    @Override
    @Transactional
    public boolean acceptFriendRequest(Long userId, Long friendId) {
        // 나(userId)에게 친구 신청한 친구(friendId)를 찾음
        List<Friend> requests = friendRepository.findByStatusAndFriendIdOrderByCreatedAtDesc(
            FriendStatus.REQUESTED, userId);

        Friend friend = requests.stream()
            .filter(f -> f.getUser().getId().equals(friendId))
            .findFirst()
            .orElse(null);

        if (friend != null) {
            friend.setStatus(FriendStatus.ACCEPTED); // 상태를 ACCEPTED로 변경
            friendRepository.save(friend);
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

        friendRepository.save(friendRequest);
        return true;
    }

    //챌린지에서 사용할 friend dto를 만들어 반환한다.
    @Override
    @Transactional(readOnly = true)
    public List<FriendResponseDTO.FriendInfoInChallengeCreate> getFrinedListInChallenge(Long userId) {
        List<Friend> friendList = friendRepository.findByStatusAndUserIdOrStatusAndFriendIdOrderByCreatedAtDesc(FriendStatus.ACCEPTED, userId, FriendStatus.ACCEPTED, userId);
        List<FriendResponseDTO.FriendInfoInChallengeCreate> dtoList = friendList.stream()
                .map(friend -> FriendConverter.toFriendInfoChallenge(friend.getFriend())) // 또는 getUser()
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
    public boolean unblockFriend(Long userId, String friendNickname) {
        User user = userService.getUserbyUserId(userId);

        // 사용자가 해당 닉네임의 친구를 차단한 관계를 찾음
        var blockedFriend = friendRepository.findByUserAndFriend_NicknameAndStatus(user, friendNickname, FriendStatus.BLOCKED);

        if (blockedFriend.isPresent()) {
            // 차단 관계를 삭제
            friendRepository.delete(blockedFriend.get());
            return true;
        }

        // 차단 관계를 찾지 못했을 때
        throw new UserException(ErrorStatus._BAD_REQUEST);
    }
}
