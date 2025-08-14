package com.planup.planup.domain.verification.service;

import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goal.service.UserGoalService;
import com.planup.planup.domain.verification.convertor.TimerVerificationConverter;
import com.planup.planup.domain.verification.repository.TimerVerificationRepository;
import com.planup.planup.domain.verification.dto.TimerVerificationResponseDto;
import com.planup.planup.domain.verification.entity.TimerVerification;
import com.planup.planup.domain.goal.repository.UserGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TimerVerificationService implements VerificationService{
    private final UserGoalRepository userGoalRepository;
    private final UserGoalService userGoalService;
    private final TimerVerificationRepository timerVerificationRepository;


    //타이머 시작 -> DB 레코드 생성(TimerVerification)
    public TimerVerificationResponseDto.TimerStartResponseDto startTimer(Long userId, Long goalId) {
        UserGoal userGoal = userGoalService.getByGoalIdAndUserId(goalId, userId);
        //에러 처리 필요

        //목표 타입 확인 필요

        List<TimerVerification> runningTimers = timerVerificationRepository
                .findByUserGoal_User_IdAndEndTimeIsNull(userId);

        if (!runningTimers.isEmpty()) {
            String runningGoalName = runningTimers.get(0).getUserGoal().getGoal().getGoalName();
            throw new RuntimeException("이미 진행중인 타이머가 있습니다: " + runningGoalName);
        }

        TimerVerification timer = TimerVerification.builder()
                .userGoal(userGoal)
                .spentTime(Duration.ZERO)
                .endTime(null)
                .build();

        TimerVerification savedTimer = timerVerificationRepository.save(timer);

        return TimerVerificationConverter.toTimerStartResponse(savedTimer);
    }

    //타이머 종료 -> 종료 시간 업데이트
    public TimerVerificationResponseDto.TimerStopResponseDto stopTimer(Long timerId, Long userId) {
        TimerVerification timer = timerVerificationRepository.findById(timerId)
                .orElseThrow(() -> new RuntimeException("타이머를 찾을 수 없습니다."));

        if (!timer.getUserGoal().getUser().getId().equals(userId)) {
            throw new RuntimeException("다른 사용자의 타이머는 종료할 수 없습니다.");
        }

        if (timer.getEndTime() != null) {
            throw new RuntimeException("이미 종료된 타이머입니다.");
        }
        // DB에 종료시간 업데이트
        LocalDateTime endTime = LocalDateTime.now();
        timer.setEndTime(endTime);
        // DB에 실제 지속 시간 업데이트
        Duration spentTime = Duration.between(timer.getCreatedAt(), endTime);
        timer.setSpentTime(spentTime);

        UserGoal userGoal = timer.getUserGoal();
        boolean achieved = isGoalAchieved(spentTime, userGoal.getGoalTime());

        //인증 횟수 증가
        if (achieved) { //achieved == true
            userGoal.increaseVerificationCount();
            userGoalRepository.save(userGoal);
        }

        TimerVerification savedTimer = timerVerificationRepository.save(timer);

        return TimerVerificationConverter.toTimerStopResponse(savedTimer, achieved);
    }

    //인증 횟수 증가 로직
    public boolean isGoalAchieved(Duration spentTime, int goalTimeMinutes) {
        //진행 중인 타이머의 경우
        if (spentTime == null) {
            return false;
        }
        //true 반환
        return spentTime.toMinutes() >= goalTimeMinutes;
    }


}
