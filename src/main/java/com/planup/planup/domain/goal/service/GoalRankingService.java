package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goal.service.policy.RankingKeyGenerator;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.query.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoalRankingService {

    private final GoalService goalService;
    private final UserQueryService userQueryService;
    private final UserGoalService userGoalService;
    private final StringRedisTemplate redisTemplate;

    public void getRankInGoal( Long userId, Long goalId) {
        User user = userQueryService.getUserByUserId(userId);
        Goal goal = goalService.getGoalById(goalId);
        UserGoal userGoal = userGoalService.getByGoalIdAndUserId(goalId, userId);
    }

    public void updateScoreOnTimerVerification(UserGoal userGoal, int verifiedSeconds) {
        Goal goal = userGoal.getGoal();
        if (goal.getVerificationType() != VerificationType.TIMER) {
            throw new IllegalArgumentException("타이머 기반 목표가 아닙니다.");
        }

        String member = String.valueOf(userGoal.getId());
        LocalDate now = LocalDate.now();

        //TODO: 랭킹 기준 확인 필요
        incrementScore(RankingKeyGenerator.goalAll(goal.getId()), member, verifiedSeconds);
        incrementScore(RankingKeyGenerator.goalDaily(goal.getId(), now), member, verifiedSeconds);
        incrementScore(RankingKeyGenerator.goalWeekly(goal.getId(), now), member, verifiedSeconds);
        incrementScore(RankingKeyGenerator.goalMonthly(goal.getId(), now), member, verifiedSeconds);
    }


    public List<RankingResult> getTopRankings(String key, int topN) {
        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, topN - 1);

        if (tuples == null) {
            return Collections.emptyList();
        }

        int rank = 1;
        List<RankingResult> results = new ArrayList<>();
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            results.add(new RankingResult(
                    rank++,
                    Long.valueOf(Objects.requireNonNull(tuple.getValue())),
                    tuple.getScore() == null ? 0 : tuple.getScore()
            ));
        }
        return results;
    }

    public Long getUserRank(String key, Long userGoalId) {
        Long reverseRank = redisTemplate.opsForZSet().reverseRank(key, String.valueOf(userGoalId));
        return reverseRank == null ? null : reverseRank + 1;
    }

    public Double getUserScore(String key, Long userGoalId) {
        return redisTemplate.opsForZSet().score(key, String.valueOf(userGoalId));
    }

    private void incrementScore(String key, String member, double score) {
        redisTemplate.opsForZSet().incrementScore(key, member, score);
    }

    public record RankingResult(
            int rank,
            Long userGoalId,
            double score
    ) {}
}
