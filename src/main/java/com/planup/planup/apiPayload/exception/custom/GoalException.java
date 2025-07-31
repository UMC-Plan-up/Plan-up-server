package com.planup.planup.apiPayload.exception.custom;

import com.planup.planup.apiPayload.code.BaseErrorCode;
import com.planup.planup.apiPayload.exception.GeneralException;

public class GoalException extends GeneralException {
    public GoalException(BaseErrorCode code) {
        super(code);
    }
}
