package com.planup.planup.domain.reaction.service;

import com.planup.planup.domain.reaction.converter.toDTO;
import com.planup.planup.domain.reaction.domain.Reaction;
import com.planup.planup.domain.reaction.domain.ReactionTargetType;
import com.planup.planup.domain.reaction.domain.ReactionType;
import com.planup.planup.domain.reaction.dto.ReactionResponseDTO;
import com.planup.planup.domain.reaction.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReactionQueryService {

    private final ReactionRepository reactionRepository;

    public List<ReactionResponseDTO> findByUserId(Long userId) {
        List<Reaction> reactionList = reactionRepository.findAllByUser_Id(userId);
        return reactionList.stream().map(toDTO::fromReactionToDTO).toList();
    }

//    public long getReactionCountByTarget(ReactionTargetType targetType, Long targetId) {
//        return reactionRepository.countByTargetTypeAndTargetId(targetType, targetId);
//    }

    public long getReactionCountByUser(Long userId) {
        return reactionRepository.countByUserId(userId);
    }
}
