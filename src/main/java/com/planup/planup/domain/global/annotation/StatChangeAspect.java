package com.planup.planup.domain.global.annotation;

import com.planup.planup.domain.user.entity.UserStat;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class StatChangeAspect {

    @Before("@annotation(com.planup.planup.domain.global.annotation.StatChanging)")
    public void markUserStatChanged(JoinPoint joinPoint) {
        Object target = joinPoint.getTarget();

        if (target instanceof UserStat) {
            ((UserStat) target).markChanged();
            log.debug("UserStat 변경 감지: changed = true");
        }
    }
}