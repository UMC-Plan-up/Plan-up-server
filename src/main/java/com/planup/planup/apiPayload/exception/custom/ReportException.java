package com.planup.planup.apiPayload.exception.custom;

import com.planup.planup.apiPayload.code.BaseErrorCode;
import com.planup.planup.apiPayload.exception.GeneralException;
import com.planup.planup.domain.report.entity.GoalReport;

public class ReportException extends GeneralException {
    public ReportException(BaseErrorCode code) {
        super(code);
    }
}
