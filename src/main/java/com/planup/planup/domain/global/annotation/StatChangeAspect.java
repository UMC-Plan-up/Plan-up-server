package com.planup.planup.domain.global.annotation;

import com.planup.planup.domain.bedge.entity.UserStat;
import com.planup.planup.domain.bedge.event.UserStatChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class StatChangeAspect {

    private final ApplicationEventPublisher eventPublisher;

    @Before("@annotation(com.planup.planup.domain.global.annotation.StatChanging)")
    public void markUserStatChanged(JoinPoint joinPoint) {
        Object target = joinPoint.getTarget();

//        if (target instanceof UserStat) {
//            ((UserStat) target).markChanged();
//            log.debug("UserStat 변경 감지: changed = true");
//        }
    }

    @After("@annotation(com.planup.planup.domain.global.annotation.StatChanging)")
    public void publishStatChangeEvent(JoinPoint joinPoint) {
        Object target = joinPoint.getTarget();
        String methodName = joinPoint.getSignature().getName();

        if (target instanceof UserStat userStat) {
            eventPublisher.publishEvent(new UserStatChangedEvent(userStat,methodName));
        }
    }
}