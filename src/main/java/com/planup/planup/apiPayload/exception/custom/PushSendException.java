package com.planup.planup.apiPayload.exception.custom;

import com.planup.planup.apiPayload.code.BaseErrorCode;
import com.planup.planup.apiPayload.exception.GeneralException;

public class PushSendException extends GeneralException {
    public PushSendException(BaseErrorCode errorCode) {super(errorCode);}
}
