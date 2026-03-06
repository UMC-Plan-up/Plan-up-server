package com.planup.planup.domain.friend.repository;

import com.planup.planup.domain.friend.entity.reportEntity.UserReportMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserReportMappingRepository extends JpaRepository<UserReportMapping, Long> {

    @Query("""
            SELECT r.reason
            FROM UserReportMapping r
            WHERE r.reported.id = :reportedId AND r.reason IS NOT NULL
            GROUP BY r.reason
            ORDER BY COUNT(r.reason) DESC
            LIMIT 1
            """)
    Optional<String> findTopReasonByReportedId(@Param("reportedId") Long reportedId);
}
