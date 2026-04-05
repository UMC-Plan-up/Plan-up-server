package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.RankingPeriod;
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

/**
 * 	특정 목표의 랭킹 점수 갱신
 * 	전체/일간/주간/월간 랭킹 저장
 * 	상위 랭킹 조회
 * 	특정 유저의 순위/점수 조회
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoalRankingService {

    private final GoalService goalService;
    private final UserQueryService userQueryService;
    private final UserGoalService userGoalService;
    private final StringRedisTemplate redisTemplate;

    private String generateKey(Long goalId, RankingPeriod period) {
        LocalDate now = LocalDate.now();

        return switch (period) {
            case ALL -> RankingKeyGenerator.goalAll(goalId);
            case DAILY -> RankingKeyGenerator.goalDaily(goalId, now);
            case WEEKLY -> RankingKeyGenerator.goalWeekly(goalId, now);
            case MONTHLY -> RankingKeyGenerator.goalMonthly(goalId, now);
        };
    }

    public void getRankInGoal( Long userId, Long goalId) {
        User user = userQueryService.getUserByUserId(userId);
        Goal goal = goalService.getGoalById(goalId);
        UserGoal userGoal = userGoalService.getByGoalIdAndUserId(goalId, userId);
    }

    //인증 업데이트에 성공했을 때 Redis에 저장된 값을 증가시킨다.
    public void updateScoreOnVerification(UserGoal userGoal, int verifiedCount) {
        Goal goal = userGoal.getGoal();

        String member = String.valueOf(userGoal.getId());
        LocalDate now = LocalDate.now();

        //TODO: 랭킹 기준 확인 필요
        incrementScore(RankingKeyGenerator.goalAll(goal.getId()), member, verifiedCount);
        incrementScore(RankingKeyGenerator.goalDaily(goal.getId(), now), member, verifiedCount);
        incrementScore(RankingKeyGenerator.goalWeekly(goal.getId(), now), member, verifiedCount);
        incrementScore(RankingKeyGenerator.goalMonthly(goal.getId(), now), member, verifiedCount);
    }

    public void deleteScoreOnVerification(UserGoal userGoal, int verifiedCount) {
        Goal goal = userGoal.getGoal();

        String member = String.valueOf(userGoal.getId());
        LocalDate now = LocalDate.now();

        //TODO: 랭킹 기준 확인 필요
        decrementScore(RankingKeyGenerator.goalAll(goal.getId()), member, verifiedCount);
        decrementScore(RankingKeyGenerator.goalDaily(goal.getId(), now), member, verifiedCount);
        decrementScore(RankingKeyGenerator.goalWeekly(goal.getId(), now), member, verifiedCount);
        decrementScore(RankingKeyGenerator.goalMonthly(goal.getId(), now), member, verifiedCount);
    }


    //Redis ZSET key에서 점수가 높은 순으로 상위 N명 랭킹을 가져온다
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

    //특정 유저(userGoalId)가 해당 랭킹 key에서 현재 몇 등인지 반환한다.
    // ex. 내순위 보기
    public Long getUserRank(String key, Long userGoalId, RankingPeriod period) {
        generateKey()
        Long reverseRank = redisTemplate.opsForZSet().reverseRank(key, String.valueOf(userGoalId));
        return reverseRank == null ? null : reverseRank + 1;
    }

    //특정 유저의 점수를 본다.
    public Double getUserScore(String key, Long userGoalId) {
        return redisTemplate.opsForZSet().score(key, String.valueOf(userGoalId));
    }

    public UserRankingInfo getUserRanking(Long goalId, Long userGoalId, RankingPeriod period) {

        Long rank = getUserRank(goalId, userGoalId, period);
        Double score = getUserScore(goalId, userGoalId, period);

        return new UserRankingInfo(rank, score);
    }

    private void incrementScore(String key, String member, double score) {
        redisTemplate.opsForZSet().incrementScore(key, member, score);
    }

    private void decrementScore(String key, String member, double score) {
        redisTemplate.opsForZSet().incrementScore(key, member, -score);
    }

    public record RankingResult(
            int rank,
            Long userGoalId,
            double score
    ) {}

    public record UserRankingInfo(
            Long rank,
            Double score
    ) {}
}
