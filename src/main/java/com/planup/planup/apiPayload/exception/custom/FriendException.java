package com.planup.planup.apiPayload.exception.custom;

import com.planup.planup.apiPayload.code.BaseErrorCode;
import com.planup.planup.apiPayload.exception.GeneralException;

public class FriendException extends GeneralException {
    public FriendException(BaseErrorCode code) {super(code);}
}
