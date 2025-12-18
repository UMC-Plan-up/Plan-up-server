package com.planup.planup.apiPayload.exception.custom;

import com.planup.planup.apiPayload.code.BaseErrorCode;
import com.planup.planup.apiPayload.exception.GeneralException;

public class TokenException extends GeneralException {
    public TokenException(BaseErrorCode code) {
        super(code);
    }
}
