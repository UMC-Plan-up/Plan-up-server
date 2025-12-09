package com.planup.planup.domain.reaction.repository;

import com.planup.planup.domain.reaction.domain.Reaction;
import com.planup.planup.domain.reaction.domain.ReactionTargetType;
import com.planup.planup.domain.reaction.domain.ReactionType;
import com.planup.planup.domain.reaction.repository.projection.ReactionCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    Optional<Reaction> findByUserIdAndTargetTypeAndTargetIdAndType(
            Long userId, ReactionTargetType targetType, Long targetId, ReactionType type);


    long countByTargetTypeAndTargetIdAndType(ReactionTargetType targetType, Long targetId, ReactionType type);

    boolean existsByUserIdAndTargetTypeAndTargetIdAndType(
            Long userId, ReactionTargetType targetType, Long targetId, ReactionType type);


    void deleteByUserIdAndTargetTypeAndTargetIdAndType(
            Long userId, ReactionTargetType targetType, Long targetId, ReactionType type);

    List<Reaction> findAllByUser_Id(Long userId);

    long countByUserId(Long userId);

    @Query("""
    select r.type, count(r)
    from Reaction r
    where r.targetType = :targetType
      and r.targetId = :targetId
    group by r.type
    """)
    List<ReactionCountProjection> countByTargetGroupedByType(
            @Param("targetType") ReactionTargetType targetType,
            @Param("targetId") Long targetId
    );

    @Query("""
    select r.type
    from Reaction r
    where r.user.id = :userId
      and r.targetType = :targetType
      and r.targetId = :targetId
    """)
    List<ReactionType> findTypesByUserAndTarget(
            @Param("userId") Long userId,
            @Param("targetType") ReactionTargetType targetType,
            @Param("targetId") Long targetId
    );
}
