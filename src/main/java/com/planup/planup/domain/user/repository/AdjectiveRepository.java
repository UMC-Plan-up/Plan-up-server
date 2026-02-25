package com.planup.planup.domain.user.repository;

import com.planup.planup.domain.user.entity.Adjective;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdjectiveRepository extends JpaRepository<Adjective, Long> {

    @Query(value = "SELECT * FROM adjective ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Adjective> findRandomAdjective();
}
