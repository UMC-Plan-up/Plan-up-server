package com.planup.planup.domain.bedge.service.userstat;

import com.planup.planup.domain.bedge.entity.SpecificGoalDays;
import com.planup.planup.domain.bedge.entity.UserStat;
import com.planup.planup.domain.user.repository.UserStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserStatCommandServiceImpl {

    private final UserStatRepository userStatRepository;
    private final UserStatQueryServiceImpl userStatQueryService;

    //특정 목표 기록
    public void recordSpecificGoal(Long userId) {
        UserStat userStat = userStatQueryService.findByUserId(userId);
        //일단 기존에 기록 중인 목표인지 확인한다.
        List<SpecificGoalDays> list = userStat.get

        //기록 중이 아닌 목표라면 새롭게 저장할 객체를 만든다

        //기록 중이라면 연속하여 기록 중인지 확인하고,
        //값을 1 또는 1 추가한다.
    }
}
