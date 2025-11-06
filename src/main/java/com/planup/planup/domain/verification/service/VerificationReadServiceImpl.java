package com.planup.planup.domain.verification.service;

import com.planup.planup.domain.report.converter.DailyRecordConverter;
import com.planup.planup.domain.report.entity.DailyRecord;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.verification.entity.PhotoVerification;
import com.planup.planup.domain.verification.entity.TimerVerification;
import com.planup.planup.domain.verification.repository.PhotoVerificationRepository;
import com.planup.planup.domain.verification.repository.TimerVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VerificationReadServiceImpl implements VerificationReadService {

    private final PhotoVerificationRepository photoVerificationRepository;
    private final TimerVerificationRepository timerVerificationRepository;

    @Override
    public List<DailyRecord> getDailyRecordForWeeklyReport(User user, LocalDateTime start, LocalDateTime end) {
        List<PhotoVerification> photoVerificationList = photoVerificationRepository.findTop5ByUserAndDateRange(user, start, end, PageRequest.of(0, 5));
        List<TimerVerification> timerVerificationList = timerVerificationRepository.findTop5ByUserAndDateRange(user, start, end, PageRequest.of(0, 5));

        List<DailyRecord> pvList = photoVerificationList.stream().map(DailyRecordConverter::PhototoDailyRecord).toList();
        List<DailyRecord> tvList = timerVerificationList.stream().map(DailyRecordConverter::TimerToRecord).toList();

        List<DailyRecord> top5DailyRecords = Stream.concat(pvList.stream(), tvList.stream())
                .sorted(Comparator.comparing(DailyRecord::getVerifiedDate).reversed())
                .limit(5).toList();

        return top5DailyRecords;
    }
}
