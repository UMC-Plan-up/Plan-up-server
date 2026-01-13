package com.planup.planup.domain.reaction.converter;

import com.planup.planup.domain.reaction.domain.Reaction;
import com.planup.planup.domain.reaction.dto.ReactionResponseDTO;

public class toDTO {
    public static ReactionResponseDTO fromReactionToDTO(Reaction reaction) {
        return ReactionResponseDTO.builder()
                .reactionId(reaction.getId())
                .userId(reaction.getUser().getId())
                .reactionType(reaction.getType())
                .targetType(reaction.getTargetType())
                .targetId(reaction.getTargetId())
                .build();
    }
}
