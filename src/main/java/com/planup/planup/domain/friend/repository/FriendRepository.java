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

    @Query("""
    select f
    from Friend f
    where f.status = :status
      and (f.user.id = :userId or f.friend.id = :userId)
    order by f.createdAt desc
""")
    List<Friend> findFriendsOfUser(
            @Param("status") FriendStatus status,
            @Param("userId") Long userId
    );

    //특정 사람과 관련된 모든 친구 관계를 반환
    @Query("""
        select f
        from Friend f
        where f.status = :status 
        and (f.id = :userId or f.id = :userId)
    """)
    List<Friend> findByStatusAndFriendIdOrderByCreatedAt(@Param("status")FriendStatus status,
                                                                     @Param("friendId")Long friendId);

    //나에게 보낸 친구 요청을 반환하되, 요청을 보낸 사람의 데이터도 같이 반환
    @Query("""
        select f
        from Friend f
        join fetch f.user u
        join fetch f.friend fr
        where f.status = :status 
        and (u.id = :userId or fr.id = :friendId)
        order by f.createdAt DESC 
    """)
    List<Friend> findByStatusAndFriendIdOrderByCreatedAtDescWithUser(@Param("status")FriendStatus status,
                                                                     @Param("friendId")Long friendId);

    //친구 관계 상태에 특정 친구 값 반환
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
    List<Friend> findListByUserIdWithUsers(@Param("status") FriendStatus status,
                                           @Param("userId") Long userId);


    @Query("""
    select (count(f) > 0)
    from Friend f
    where f.status = :status
      and ((f.user.id = :userId and f.friend.id = :friendId)
        or (f.user.id = :friendId and f.friend.id = :userId))
""")
    Boolean existsByUsersAndStatus(@Param("userId") Long userId,
                                   @Param("friendId") Long friendId,
                                   @Param("status") FriendStatus status);


    Boolean existsByUserAndFriendAndStatus(User user, User friend, FriendStatus status);
}
