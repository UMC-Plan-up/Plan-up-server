package com.planup.planup.domain.reaction.repository;

import com.planup.planup.domain.reaction.domain.Reaction;
import com.planup.planup.domain.reaction.domain.ReactionTargetType;
import com.planup.planup.domain.reaction.domain.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    Optional<Reaction> findByUserIdAndTargetTypeAndTargetIdAndType(
            Long userId, ReactionTargetType targetType, Long targetId, ReactionType type);

    long countByTargetTypeAndTargetIdAndType(
            ReactionTargetType targetType, Long targetId, ReactionType type);

    void deleteByUserIdAndTargetTypeAndTargetIdAndType(
            Long userId, ReactionTargetType targetType, Long targetId, ReactionType type);

    List<Reaction> findAllByUser_Id(Long userId);

    long countByUserId(Long userId);
}
