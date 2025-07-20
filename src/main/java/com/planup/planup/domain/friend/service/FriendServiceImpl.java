package com.planup.planup.domain.friend.service;

import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.friend.entity.FriendStatus;
import com.planup.planup.domain.friend.dto.FriendResponseDTO;
import com.planup.planup.domain.friend.repository.FriendRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.planup.planup.domain.friend.converter.FriendConverter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final FriendRepository friendRepository;
    private final UserService userService;

    @Override
    public List<FriendResponseDTO.FriendSummaryList> getFriendSummeryList(Long userId) {

        User user = userService.getUserbyUserId(userId);

        List<Friend> friendList = friendRepository.findByStatusAndUserIdOrStatusAndFriendIdOrderByCreatedAtDesc(FriendStatus.ACCEPTED, user.getId(), FriendStatus.ACCEPTED, user.getId());

        //Friend 객체의 friend, user 중 내가 아닌 친구를 뽑는다.
        List<User> list = friendList.stream()
                .map(f -> f.getFriend().equals(user) ? f.getUser() : f.getFriend())
                .collect(Collectors.toList());


        return null;
    }

    @Override
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
        return false;
    }

    @Override
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
        return false;
    }

    @Override
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
        return false;
    }

    @Override
    public List<FriendResponseDTO.FriendInfoSummary> getRequestedFriends(Long userId) {
        List<Friend> friendRequests = friendRepository.findByStatusAndFriendIdOrderByCreatedAtDesc(
            FriendStatus.REQUESTED, userId);

        // 신청한 친구(User) 정보 추출
        return friendRequests.stream()
                .map(f -> FriendConverter.toFriendSummary(f.getUser()))
                .collect(Collectors.toList());
    }
}
