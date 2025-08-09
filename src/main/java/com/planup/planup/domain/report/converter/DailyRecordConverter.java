package com.planup.planup.domain.report.converter;

import com.planup.planup.domain.report.entity.DailyRecord;
import com.planup.planup.domain.report.entity.WeeklyReport;
import com.planup.planup.domain.verification.entity.PhotoVerification;
import com.planup.planup.domain.verification.entity.TimerVerification;

public class DailyRecordConverter {

    public static DailyRecord PhototoDailyRecord(PhotoVerification photoVerification) {
        return DailyRecord.builder()
                .recordedTime(0)
                .photoVerified(photoVerification.getPhotoImgs().get(0))
                .verifiedDate(photoVerification.getCreatedAt())
                .simpleMessage(null)
                .build();
    }

    public static DailyRecord TimerToRecord(TimerVerification timerVerification) {
        return DailyRecord.builder()
                .recordedTime(timerVerification.getSpentTime().toSeconds())
                .photoVerified(null)
                .verifiedDate(timerVerification.getCreatedAt())
                .simpleMessage(null)
                .build();
    }
}
