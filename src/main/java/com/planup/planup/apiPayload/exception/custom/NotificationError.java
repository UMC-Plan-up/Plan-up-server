package com.planup.planup.apiPayload.exception.custom;

import com.planup.planup.apiPayload.code.BaseErrorCode;
import com.planup.planup.apiPayload.exception.GeneralException;

public class NotificationError extends GeneralException {
    public NotificationError(BaseErrorCode code) {
        super(code);
    }
}
