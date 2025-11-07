package com.planup.planup.domain.goal.dto;

import com.planup.planup.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserWithGoalCountDTO {
    private User user;
    private Long goalCnt;
}
