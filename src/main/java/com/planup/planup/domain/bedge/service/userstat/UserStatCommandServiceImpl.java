package com.planup.planup.domain.bedge.service.userstat;

import com.planup.planup.domain.bedge.entity.SpecificGoalDays;
import com.planup.planup.domain.bedge.entity.UserStat;
import com.planup.planup.domain.bedge.repository.SpecificGoalDaysRepository;
import com.planup.planup.domain.goal.service.GoalService;
import com.planup.planup.domain.user.repository.UserStatRepository;
import com.planup.planup.domain.user.service.query.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserStatCommandServiceImpl {

    private final UserStatRepository userStatRepository;
    private final UserStatQueryServiceImpl userStatQueryService;
    private final GoalService goalService;
    private final UserQueryService userQueryService;
    private final SpecificGoalDaysRepository specificGoalDaysRepository;

    //특정 목표 기록
    public void recordSpecificGoal(Long userId, Long goalId) {
        UserStat userStat = userStatQueryService.findByUserIdWithSpecificDays(userId);

        //일단 기존에 기록 중인 목표인지 확인한다.
        List<SpecificGoalDays> list = userStat.getRecordAllGoal7Days();
        List<SpecificGoalDays> filteredList = list.stream().filter(sd -> sd.getGoal().getId().equals(goalId) && (sd.getLastUpdate().equals(LocalDate.now()) || sd.getLastUpdate().equals(LocalDate.now().minusDays(1)))).toList();
        SpecificGoalDays sd = null;
        if (filteredList.size() > 1) sd = filteredList.get(0);

        //기록 중이 아닌 목표라면 새롭게 저장할 객체를 만든다
        if (sd == null) {
            createNewSpecificGoal(userId, goalId, userStat);
            return;
        }

        //기록 중이라면 연속하여 기록 중인지 확인하고, 값을 업데이트하거나 기존 데이터를 삭제하고 새로운 데이터를 업데이트한다.
        if (isConsecutiveSuccessRecord(sd) && isAlreadyUpdatedGoal(sd)) {
            sd.updateNewRecord();
        } else {
            specificGoalDaysRepository.delete(sd);
            createNewSpecificGoal(userId, goalId, userStat);
        }
    }

    private void createNewSpecificGoal(Long userId, Long goalId, UserStat userStat) {
        SpecificGoalDays specificGoalDays = SpecificGoalDays.builder()
                .goal(goalService.getGoalById(goalId))
                .user(userQueryService.getUserByUserId(userId))
                .lastUpdate(LocalDate.now())
                .consecutiveSuccessDays(1)
                .build();

        userStat.getRecordAllGoal7Days().add(specificGoalDays);
    }

    public Boolean isConsecutiveSuccessRecord(SpecificGoalDays sd) {
        return sd.getLastUpdate().equals(LocalDate.now()) || sd.getLastUpdate().equals(LocalDate.now().minusDays(1));
    }

    public Boolean isAlreadyUpdatedGoal(SpecificGoalDays sd) {
        return sd.getLastUpdate().equals(LocalDate.now());
    }
}
