package com.planup.planup.domain.friend.repository;

import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.friend.entity.FriendStatus;
import com.planup.planup.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    List<Friend> findByStatusAndUserIdOrStatusAndFriendIdOrderByCreatedAtDesc(
            FriendStatus s1, Long u1, FriendStatus s2, Long u2);

    List<Friend> findByStatusAndFriend_IdOrderByCreatedAtDesc(
            FriendStatus status, Long friendId
    );

    // 사용자가 차단한 친구 목록 조회
    List<Friend> findByUserAndStatusOrderByCreatedAtDesc(User user, FriendStatus status);

    // 특정 사용자가 특정 친구를 차단한 관계 조회 (닉네임으로)
    Optional<Friend> findByUserAndFriend_NicknameAndStatus(User user, String friendNickname, FriendStatus status);

    Boolean existsByUserAndFriendAndStatus(User user, User friend, FriendStatus status);
}
