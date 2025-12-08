package com.planup.planup.domain.reaction.service;

import com.planup.planup.domain.reaction.domain.Reaction;
import com.planup.planup.domain.reaction.domain.ReactionTargetType;
import com.planup.planup.domain.reaction.domain.ReactionType;
import com.planup.planup.domain.reaction.repository.ReactionRepository;
import com.planup.planup.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReactionCommandService {

    private final ReactionRepository reactionRepository;

    public Reaction createReaction(User user, ReactionType reactionType, ReactionTargetType targetType, Long targetId) {
        Reaction reaction = Reaction.builder()
                .user(user)
                .targetType(targetType)
                .type(reactionType)
                .targetId(targetId)
                .build();

        reactionRepository.save(reaction);
        return reaction;
    }


}
