package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalLifeService {

    private final GoalRepository goalRepository;

    @Transactional
    public void disableExpiredGoals(Date date) {
        List<Goal> goal = goalRepository.findByEndDateBeforeAndIsActiveTrue(date);
        goal.forEach(Goal::setInActive);
    }
}


