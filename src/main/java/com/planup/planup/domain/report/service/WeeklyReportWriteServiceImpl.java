package com.planup.planup.domain.report.service;

import com.planup.planup.domain.report.converter.DailyRecordConverter;
import com.planup.planup.domain.report.entity.DailyRecord;
import com.planup.planup.domain.report.entity.GoalMessage;
import com.planup.planup.domain.report.entity.GoalReport;
import com.planup.planup.domain.report.entity.WeeklyReport;
import com.planup.planup.domain.report.repository.WeeklyReportRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.verification.entity.PhotoVerification;
import com.planup.planup.domain.verification.entity.TimerVerification;
import com.planup.planup.domain.verification.service.VerificationReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
public class WeeklyReportWriteServiceImpl implements WeeklyReportWriteService {

    private final GoalReportService goalReportService;
    private final VerificationReadService verificationReadService;

    private final WeeklyReportRepository weeklyReportRepository;


    @Override
    @Transactional
    public void createWeeklyReport(User user, LocalDateTime startDate) {
        Long userId = user.getId();

        //리포트 작성일을 기준으로 가장 최근 일주일의 값을 지정한다. 이때 작성된 GOALREPORT를 조회
        LocalDateTime startOfDay = startDate.toLocalDate().atStartOfDay(); // 2025-08-07T00:00
        LocalDateTime endOfDay= startDate.plusDays(6).toLocalDate().atTime(23, 59, 59);  // 일요일 23:59

        //weeklyReport에 들어갈 리포트 조회
        List<GoalReport> goalReportList = goalReportService.getListByUserIdOneDay(userId, startOfDay, endOfDay);

        //weeklyReport에 들어갈 레코드 5개 조회
        List<DailyRecord> dailyRecordForWeeklyReport = verificationReadService.getDailyRecordForWeeklyReport(user, startOfDay, endOfDay);

        WeeklyReport weeklyReport = WeeklyReport.builder()
                .user(user) // User 엔티티
                .year(startDate.getYear())
                .month(startDate.getMonthValue())
                .weekNumber(startDate.get(WeekFields.of(Locale.KOREA).weekOfMonth()))
                .startDate(startDate)
                .endDate(endOfDay)
                //TODO: goalMessage 찾아오기
                .nextGoalMessage(GoalMessage.KEEP_GOING) // Enum 값
                .goalReports(goalReportList) // 초기 리스트
                .dailyRecords(dailyRecordForWeeklyReport) // 초기 리스트
                .build();

        weeklyReportRepository.save(weeklyReport);
    }
}
