package com.planup.planup.domain.friend.repository;

import com.planup.planup.domain.friend.entity.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    @Query("""
      select case when count(b) > 0 then true else false end
      from UserBlock b
      where (b.blocker.id = :u1 and b.blocked.id = :u2)
         or (b.blocker.id = :u2 and b.blocked.id = :u1)
    """)
    boolean existsEitherDirection(Long u1, Long u2);

    @Query("""
        select b
        from UserBlock b
        join fetch b.blocked blocked
        join fetch b.blocker blocker
        where (b.blocker.id = :blockerId)
        and (b.active = true)
    """)
    List<UserBlock> findBlockedByBlockerId(Long blockerId);

    Optional<UserBlock> findByBlockerIdAndBlockedId(Long userId, Long friendId);

    @Query("""
    select b
    from UserBlock b
    join fetch b.blocked blocked
    where b.blocker.id = :userId
      and b.blocked.id = :friendId
""")
    Optional<UserBlock> findByUserIdAndFriendIdWithBlocked(@Param("userId") Long userId,
                                                           @Param("friendId") Long friendId);
}
