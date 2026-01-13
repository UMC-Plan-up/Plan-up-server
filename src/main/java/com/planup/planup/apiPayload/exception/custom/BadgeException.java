package com.planup.planup.apiPayload.exception.custom;

import com.planup.planup.apiPayload.code.BaseErrorCode;
import com.planup.planup.apiPayload.exception.GeneralException;

public class BadgeException extends GeneralException {
    public BadgeException(BaseErrorCode code) {
        super(code);
    }
}
