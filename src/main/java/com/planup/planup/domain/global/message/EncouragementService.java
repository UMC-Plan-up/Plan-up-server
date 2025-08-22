package com.planup.planup.domain.global.message;

import com.planup.planup.domain.goal.dto.UserGoalResponseDto;
import com.planup.planup.domain.goal.service.GoalService;
import com.planup.planup.domain.goal.service.UserGoalAggregationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EncouragementService {

    private final WebClient webClient;
    private final String endpoint;
    private final String model;
    private final UserGoalAggregationService userGoalAggregationService;
    private final GoalService goalService;

    public EncouragementService(
            WebClient.Builder builder,
            @Value("${gemini.endpoint}") String endpoint,
            @Value("${gemini.model}") String model,
            @Value("${gemini.api-key}") String apiKey,
            UserGoalAggregationService userGoalAggregationService,
            GoalService goalService
    ) {
        this.webClient = builder
                .baseUrl(endpoint)
                .defaultHeader("x-goog-api-key", apiKey)
                .build();
        this.endpoint = endpoint;
        this.model = model;
        this.userGoalAggregationService = userGoalAggregationService;
        this.goalService = goalService;
    }

    public Mono<MessageResponse> generate(MessageRequest req) {
        int achievementRate = userGoalAggregationService.getDailyAchievement(req.userId(), LocalDate.now().minusDays(1)).getAchievementRate();

        Map<String, Integer> goalAchList = new HashMap<>();

        List<UserGoalResponseDto.GoalTotalAchievementDto> dtoList = req.goalIdList().stream()
                .map(goalId -> userGoalAggregationService.getTotalAchievement(goalId, req.userId()))
                .toList();

        // Map에 값 넣기
        dtoList.forEach(dto -> goalAchList.put(
                dto.getGoalId().toString(),
                dto.getTotalAchievementRate()
        ));


        String prompt = buildPrompt(
                req.name(),
                req.context(),
                "밝게",
                "존댓말",
                true,
                achievementRate,
                goalAchList
        );

        Map<String, Object> body = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{ Map.of("text", prompt) })
                },
                "generationConfig", Map.of(
                        "maxOutputTokens", 128,
                        "temperature", 0.8
                )
        );

        String path = String.format("/%s:generateContent", model);

        return webClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::extractText)        // Gemini 응답 → 텍스트
                .map(MessageResponse::new);
    }

    private String extractText(Map<?, ?> resp) {
        // 응답: candidates[0].content.parts[*].text 를 이어붙임
        try {
            var candidates = (java.util.List<?>) resp.get("candidates");
            if (candidates == null || candidates.isEmpty()) return "";
            var cand0 = (Map<?, ?>) candidates.get(0);
            var content = (Map<?, ?>) cand0.get("content");
            var parts = (java.util.List<?>) content.get("parts");
            StringBuilder sb = new StringBuilder();
            for (Object p : parts) {
                var m = (Map<?, ?>) p;
                var t = (String) m.get("text");
                if (StringUtils.hasText(t)) sb.append(t);
            }
            // 따옴표/개행 정리
            return sb.toString().replaceAll("^[\"“”‘’\\s]+|[\"“”‘’\\s]+$", "");
        } catch (Exception e) {
            return "";
        }
    }

    private String buildPrompt(
            String name,
            String context,
            String tone,
            String formality,
            boolean emoji,
            int dailyAchievementRate, // 일일 성취율 %
            Map<String, Integer> goalAchievementRates // 목표별 성취율
    ) {
        return """
            시스템: 너는 따뜻하고 센스있는 카피라이터야.
            규칙:
            - 한국어로만 작성.
            - 최대 2문장, 80자 이내.
            - 톤: %s
            - 존댓말 사용 필수: "[이름]님"으로 호칭하고 존댓말로 작성
            - 이모지 사용: %s (true면 1~2개 사용)
            - 비속어/혐오/민감 조언 금지, 반복 피하기
            참고 데이터:
            - 어제의 전체 성취율: %d%%
            - 목표별 성취율: %s
            컨텍스트:
            - 받는 사람: %s
            - 상황/목표: %s
            출력: "[이름]님"으로 시작하는 존댓말 응원 메시지 한 개만 반환. 따옴표 없이 본문만.
            """.formatted(
                tone,
                emoji,
                dailyAchievementRate,
                goalAchievementRates.toString(),
                name,
                context
        ).trim();
    }
}
