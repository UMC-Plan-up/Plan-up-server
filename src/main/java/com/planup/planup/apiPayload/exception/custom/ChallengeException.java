package com.planup.planup.apiPayload.exception.custom;

import com.planup.planup.apiPayload.code.BaseErrorCode;
import com.planup.planup.apiPayload.exception.GeneralException;

public class ChallengeException extends GeneralException {
    public ChallengeException(BaseErrorCode code) {
        super(code);
    }
}
