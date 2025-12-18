package com.planup.planup.domain.reaction.repository.projection;

import com.planup.planup.domain.reaction.domain.ReactionType;

public interface ReactionCountProjection {
    ReactionType getType();
    long getCount();
}