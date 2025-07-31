package com.planup.planup.apiPayload.exception.custom;

import com.planup.planup.apiPayload.code.BaseErrorCode;

public class UserGoalException extends GoalException{
    public UserGoalException(BaseErrorCode code) {
        super(code);
    }
}
