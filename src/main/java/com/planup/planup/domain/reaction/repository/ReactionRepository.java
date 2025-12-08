package com.planup.planup.domain.reaction.repository;

import com.planup.planup.domain.reaction.domain.Reaction;
import com.planup.planup.domain.reaction.domain.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {


}
