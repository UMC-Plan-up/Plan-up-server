package com.planup.planup.domain.global.message;

import com.planup.planup.domain.goal.dto.UserGoalResponseDto;
import com.planup.planup.domain.goal.service.UserGoalAggregationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EncouragementService {

    private final WebClient webClient;
    private final String endpoint;
    private final String model;
    private final UserGoalAggregationService userGoalAggregationService;

    public EncouragementService(
            WebClient.Builder builder,
            @Value("${gemini.endpoint}") String endpoint,
            @Value("${gemini.model}") String model,
            @Value("${gemini.api-key}") String apiKey,
            UserGoalAggregationService userGoalAggregationService
    ) {
        this.webClient = builder
                .baseUrl(endpoint)
                .defaultHeader("x-goog-api-key", apiKey)
                .build();
        this.endpoint = endpoint;
        this.model = model;
        this.userGoalAggregationService = userGoalAggregationService;
    }

    public Mono<MessageResponse> generate(MessageRequest req) {
        // 간단한 응원 메시지 생성 (성취율 비교 제외)
        Map<String, Integer> goalAchList = new HashMap<>();

        List<UserGoalResponseDto.GoalTotalAchievementDto> dtoList = req.goalIdList().stream()
                .map(goalId -> userGoalAggregationService.getTotalAchievement(goalId, req.userId()))
                .toList();

        // Map에 값 넣기
        dtoList.forEach(dto -> goalAchList.put(
                dto.getGoalId().toString(),
                dto.getTotalAchievementRate()
        ));

        String prompt = buildSimplePrompt(
                req.name(),
                req.context(),
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
                .map(MessageResponse::new)
                .onErrorReturn(new MessageResponse(getDefaultMessage(req.name(), req.context())));
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

    private String buildSimplePrompt(
            String name,
            String context,
            Map<String, Integer> goalAchievementRates // 목표별 성취율
    ) {
        return """
            시스템: 너는 따뜻하고 센스있는 카피라이터야.
            규칙:
            - 한국어로만 작성.
            - 최대 2문장, 80자 이내.
            - 톤: 밝게
            - 존댓말 사용 필수: "[이름]님"으로 호칭하고 존댓말로 작성
            - 이모지 사용: 1~2개 사용
            - 비속어/혐오/민감 조언 금지, 반복 피하기
            - 성취율이 낮아도 진정성 있게 격려하고, 비꼬는 듯한 표현 금지
            
            참고 데이터:
            - 목표별 성취율: %s
            
            메시지 생성 방향:
            - 성취율이 높은 경우: 성취를 축하하고 격려
            - 성취율이 낮거나 0%%인 경우: 공감하고 작은 진전도 의미있다고 위로
            - 구체적인 수치를 자연스럽게 포함하되, 수치에만 집중하지 말고 인간적인 관점에서 접근
            - 사용자의 상황(%s)과 연결하여 개인화된 메시지 생성
            
            컨텍스트:
            - 받는 사람: %s
            - 상황/목표: %s
            
            출력: "[이름]님"으로 시작하는 존댓말 응원 메시지 한 개만 반환. 따옴표 없이 본문만.
            """.formatted(
                goalAchievementRates.toString(),
                context,
                name,
                context
        ).trim();
    }

    private String getDefaultMessage(String name, String context) {
        // AI 메시지 생성 실패 시 반환할 기본 메시지
        String[] defaultMessages = {
            "%s님, %s 정말 열심히 하고 계시네요! 💪 꾸준히 노력하는 모습이 정말 대단해요! ✨",
            "%s님, %s 긍정적인 마음으로 차근차근 준비하면 분명 좋은 결과가 있을 거예요! 💪",
            "%s님, %s 목표를 향해 꾸준히 나아가는 모습이 정말 멋져요! 화이팅입니다! ✨",
            "%s님, %s 작은 진전도 큰 의미가 있어요! 계속해서 노력해주세요! 💪",
            "%s님, %s 어려운 시간이지만 당신의 노력을 응원합니다! 힘내세요! ✨",
            "%s님, 오늘 10분만 투자해도 오늘 하루가 달라질 거예요. 작은 습관이 큰 차이를 만들어요 ☀️"
        };
        
        // 랜덤하게 메시지 선택
        int randomIndex = (int) (Math.random() * defaultMessages.length);
        return String.format(defaultMessages[randomIndex], name, context);
    }
}
