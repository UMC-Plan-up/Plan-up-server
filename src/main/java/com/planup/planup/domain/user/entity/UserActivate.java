package com.planup.planup.domain.user.entity;

public enum UserActivate {
    ACTIVE("활성화"),
    INACTIVE("비활성화"),
    DELETED("삭제");

    private final String description;

    UserActivate(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
