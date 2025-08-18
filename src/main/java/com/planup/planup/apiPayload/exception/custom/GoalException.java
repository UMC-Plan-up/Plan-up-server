package com.planup.planup.apiPayload.exception.custom;

import com.planup.planup.apiPayload.code.BaseErrorCode;
import com.planup.planup.apiPayload.exception.GeneralException;

public class GoalException extends GeneralException {
    private String customMessage;
    public GoalException(BaseErrorCode code) {
        super(code);
    }

    public GoalException setCustomMessage(String customMessage) {
        this.customMessage = customMessage;
        return this;
    }

    public String getCustomMessage() {
        if (customMessage != null && !customMessage.trim().isEmpty()) {
            return customMessage;
        }
        return super.getErrorReason().getMessage();
    }
}
