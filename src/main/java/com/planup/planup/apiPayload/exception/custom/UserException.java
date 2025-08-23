package com.planup.planup.apiPayload.exception.custom;

import com.planup.planup.apiPayload.code.BaseErrorCode;
import com.planup.planup.apiPayload.exception.GeneralException;

public class UserException extends GeneralException {
    private final BaseErrorCode errorCode;

    public UserException(BaseErrorCode code) {
        super(code);
        this.errorCode = code;
    }

    public BaseErrorCode getErrorStatus() {
        return this.errorCode;
    }
}
