package com.planup.planup.domain.bedge.event;

import com.planup.planup.domain.bedge.entity.UserStat;

public class UserStatChangedEvent {

    private final UserStat userStat;
    private final String methodName;

    public UserStatChangedEvent(UserStat userStat, String methodName) {
        this.userStat = userStat;
        this.methodName = methodName;
    }

    public UserStat getUserStat() {
        return userStat;
    }

    public String getMethodName() {
        return methodName;
    }
}
