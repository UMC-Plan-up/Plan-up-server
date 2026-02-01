package com.planup.planup.domain.user.repository;

import com.planup.planup.domain.user.entity.Noun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NounRepository extends JpaRepository<Noun, Long> {

    @Query(value = "SELECT * FROM noun ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Noun> findRandomNoun();
}
