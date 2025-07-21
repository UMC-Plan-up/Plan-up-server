package com.planup.planup.domain.report.repository;

import com.planup.planup.domain.report.entity.WeeklyReport;
import com.planup.planup.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Long> {

    List<WeeklyReport> findByUserAndYearAndMonth(User user, int year, int month);
}
