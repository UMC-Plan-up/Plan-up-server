package com.planup.planup.domain.friend.repository;

import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.friend.entity.FriendStatus;
import com.planup.planup.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    List<Friend> findByStatusAndUserIdOrStatusAndFriendIdOrderByCreatedAtDesc(
            FriendStatus s1, Long u1, FriendStatus s2, Long u2);

    List<Friend> findByStatusAndFriendIdOrderByCreatedAtDesc(FriendStatus status, Long friendId);

    // 사용자가 차단한 친구 목록 조회
    List<Friend> findByUserAndStatusOrderByCreatedAtDesc(User user, FriendStatus status);
}
