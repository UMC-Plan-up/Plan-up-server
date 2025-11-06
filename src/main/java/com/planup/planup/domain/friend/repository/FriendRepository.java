package com.planup.planup.domain.friend.repository;

import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.friend.entity.FriendStatus;
import com.planup.planup.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    //나에게 보낸 친구 요청을 반환하되, 요청을 보낸 사람의 데이터도 같이 반환
    @Query("""
        select f
        from Friend f
        join fetch f.user u
        join fetch f.friend fr
        where f.status = :status and fr.id = :friendId
    """)
    List<Friend> findByStatusAndFriendIdOrderByCreatedAtDescWithUser(FriendStatus status, Long friendId);

    // 사용자가 차단한 친구 목록 조회
    List<Friend> findByUserAndStatusOrderByCreatedAtDesc(User user, FriendStatus status);

    // 특정 사용자가 특정 친구를 차단한 관계 조회 (닉네임으로)
    Optional<Friend> findByUserAndFriend_NicknameAndStatus(User user, String friendNickname, FriendStatus status);

    //친구 관계 상태에 따라 친구 리스트를 반환
    @Query("""
        select f
        from Friend f
        where f.status = :status
          and ((f.user.id = :userId and f.friend.id = :friendId)
          or (f.user.id = :friendId and f.friend.id = :userId))
    """)
    Optional<Friend> findByUserIdAndFriendIdAndStatus(@Param("status") FriendStatus status,
                                                      @Param("userId") Long userId,
                                                      @Param("friendId") Long friendId);

    //친구 관계 상태에 따라 친구 리스트를 반환(유저 정보도 같이 정리)
    @Query("""
        select f
        from Friend f
        join fetch f.user u
        join fetch f.friend fr
        where f.status = :status
          and (u.id = :userId or fr.id = :userId)
        order by f.createdAt desc
    """)
    List<Friend> findAcceptedByUserIdWithUsers(@Param("status") FriendStatus status,
                                               @Param("userId") Long userId);

    @Query("""
        select f
        from Friend f
        where f.status <> :status
        and ((f.user.id = :userId and f.friend.id = :friendId)
          or (f.user.id = :friendId and f.friend.id = :userId))
    """)
    Optional<Friend> findByUserIdAndFriendIdAndStatusNot(@Param("userId") Long userId,
                                                         @Param("friendId") Long friendId,
                                                         @Param("status") FriendStatus status);
}
