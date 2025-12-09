package com.planup.planup.domain.reaction.dto;

import com.planup.planup.domain.reaction.domain.ReactionTargetType;
import com.planup.planup.domain.reaction.domain.ReactionType;
import lombok.Builder;

@Builder
public record ReactionResponseDTO(
        Long reactionId,
        Long userId,
        ReactionType reactionType,
        ReactionTargetType targetType,
        Long targetId
) {}
