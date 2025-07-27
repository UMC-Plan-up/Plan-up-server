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

    @Override
    @Transactional(readOnly = true) 
    public List<FriendResponseDTO.FriendSummaryList> getFriendSummeryList(Long userId) {

        User user = userService.getUserbyUserId(userId);

        List<Friend> friendList = friendRepository.findByStatusAndUserIdOrStatusAndFriendIdOrderByCreatedAtDesc(FriendStatus.ACCEPTED, user.getId(), FriendStatus.ACCEPTED, user.getId());

        //Friend 객체의 friend, user 중 내가 아닌 친구를 뽑는다.
        List<User> list = friendList.stream()
                .map(f -> f.getFriend().equals(user) ? f.getUser() : f.getFriend())
                .collect(Collectors.toList());


        return List.of(FriendConverter.toFriendSummaryList(list));
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
        // 1. Friend 엔티티 찾기
        List<Friend> friends = friendRepository.findByStatusAndUserIdOrStatusAndFriendIdOrderByCreatedAtDesc(
            FriendStatus.ACCEPTED, userId, FriendStatus.ACCEPTED, userId);

        Friend friend = friends.stream()
            .filter(f -> (f.getUser().getId().equals(friendId) || f.getFriend().getId().equals(friendId)))
            .findFirst()
            .orElse(null);

        if (friend != null) {
            // 신고 사유 저장 (별도 테이블이 있다면 Report 엔티티에 저장, 없다면 로그 등)
            System.out.println("신고 사유: " + reason);

            // 차단 여부에 따라 상태 변경
            if (block) {
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
        User friend = userService.getUserbyUserId(friendId);

        // Friend 엔티티 생성
        Friend friendRequest = new Friend();
        friendRequest.setUser(user);
        friendRequest.setFriend(friend);
        friendRequest.setStatus(FriendStatus.REQUESTED);

        friendRepository.save(friendRequest);
        return true;
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
                            .friendNickname(blockedUser.getNickname())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
