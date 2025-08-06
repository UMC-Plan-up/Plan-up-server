package com.planup.planup.domain.report.repository;

import com.planup.planup.domain.report.entity.ReportUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportUserRepository extends JpaRepository<ReportUser, Long> {

}
