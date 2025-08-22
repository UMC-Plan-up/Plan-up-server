package com.planup.planup.domain.global.message;

import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goal.service.UserGoalAggregationService;
import com.planup.planup.domain.goal.service.UserGoalService;
import com.planup.planup.domain.notification.dto.NotificationResponseDTO;
import com.planup.planup.domain.notification.service.NotificationService;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EncouragementService {

    private final WebClient webClient;
    private final String endpoint;
    private final String model;
    private final UserGoalAggregationService userGoalAggregationService;
    private final UserGoalService userGoalService;
    private final NotificationService notificationService;
    private final UserService userService;
    private final DailyLimitService dailyLimitService;

    public EncouragementService(
            WebClient.Builder builder,
            @Value("${gemini.endpoint}") String endpoint,
            @Value("${gemini.model}") String model,
            @Value("${gemini.api-key}") String apiKey,
            UserGoalAggregationService userGoalAggregationService,
            UserGoalService userGoalService,
            NotificationService notificationService,
            UserService userService,
            DailyLimitService dailyLimitService
    ) {
        this.webClient = builder
                .baseUrl(endpoint)
                .defaultHeader("x-goog-api-key", apiKey)
                .build();
        this.endpoint = endpoint;
        this.model = model;
        this.userGoalAggregationService = userGoalAggregationService;
        this.userGoalService = userGoalService;
        this.notificationService = notificationService;
        this.userService = userService;
        this.dailyLimitService = dailyLimitService;
    }

    public Mono<MessageResponse> generate(Long userId) {
        // Redis 기반 일일 제한 체크 - 이미 받은 경우 저장된 메시지 반환
        if (dailyLimitService.hasReceivedToday(userId)) {
            String savedMessage = dailyLimitService.getTodayMessage(userId);
            // Redis에서 null이 반환되거나 빈 문자열인 경우 새로운 메시지 생성
            if (savedMessage == null || savedMessage.trim().isEmpty()) {
                // Redis 데이터 정리 후 새로운 메시지 생성
                dailyLimitService.clearTodayData(userId);
            } else {
                return Mono.just(new MessageResponse(savedMessage));
            }
        }
        
        // 사용자 데이터 수집
        UserData userData = collectUserData(userId);
        
        // AI 메시지 생성
        String prompt = buildPrompt(userData);

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
                .map(this::extractText)
                .map(text -> {
                    // 빈 문자열인 경우 기본 메시지 사용
                    if (text == null || text.trim().isEmpty()) {
                        return getDefaultMessage(userData);
                    }
                    return text;
                })
                .map(MessageResponse::new)
                .doOnSuccess(response -> dailyLimitService.markAsReceived(userId, response.message()))
                .onErrorReturn(new MessageResponse(getDefaultMessage(userData)))
                .doOnSuccess(response -> {
                    // 에러로 인한 기본 메시지인 경우에도 저장
                    if (response.message().equals(getDefaultMessage(userData))) {
                        dailyLimitService.markAsReceived(userId, response.message());
                    }
                });
    }

    private UserData collectUserData(Long userId) {
        LocalDate today = LocalDate.now();
        
        // 사용자 정보
        User user = userService.getUserbyUserId(userId);
        
        // 활성 목표 목록
        List<UserGoal> activeGoals = userGoalService.getActiveUserGoalsByUser(userId, today);
        
        // 목표별 성취율 계산
        Map<Long, Integer> goalAchievements = activeGoals.stream()
                .collect(Collectors.toMap(
                        ug -> ug.getGoal().getId(),
                        ug -> userGoalAggregationService.getTotalAchievement(ug.getGoal().getId(), userId).getTotalAchievementRate()
                ));
        
        // 주간 성취율 비교
        int thisWeekAchievement = userGoalAggregationService.getDailyAchievement(userId, today).getAchievementRate();
        int lastWeekAchievement = userGoalAggregationService.getDailyAchievement(userId, today.minusWeeks(1)).getAchievementRate();
        
        // 최근 알림
        List<NotificationResponseDTO.NotificationDTO> recentNotifications = notificationService.getTop5RecentByUser(userId);
        
        // 추가 데이터 계산
        int consecutiveDays = calculateConsecutiveDays(userId, today);
        int recentCommentCount = countRecentComments(recentNotifications);
        List<String> friendAchievements = extractFriendAchievements(recentNotifications);
        
        return new UserData(userId, activeGoals, goalAchievements, thisWeekAchievement, lastWeekAchievement, 
                           recentNotifications, consecutiveDays, recentCommentCount, friendAchievements);
    }

    // 연속 성공 일수 계산
    private int calculateConsecutiveDays(Long userId, LocalDate today) {
        int consecutiveDays = 0;
        for (int i = 0; i < 7; i++) {
            LocalDate checkDate = today.minusDays(i);
            int achievement = userGoalAggregationService.getDailyAchievement(userId, checkDate).getAchievementRate();
            if (achievement > 0) {
                consecutiveDays++;
            } else {
                break;
            }
        }
        return consecutiveDays;
    }

    // 최근 받은 댓글 수 계산
    private int countRecentComments(List<NotificationResponseDTO.NotificationDTO> notifications) {
        return (int) notifications.stream()
                .filter(n -> n.notificationText().contains("댓글") || n.notificationText().contains("응원"))
                .count();
    }

    // 친구 성취 목록 추출
    private List<String> extractFriendAchievements(List<NotificationResponseDTO.NotificationDTO> notifications) {
        return notifications.stream()
                .filter(n -> {
                    String text = n.notificationText();
                    return text.contains("목표") || text.contains("챌린지") || text.contains("친구") || 
                           text.contains("완료") || text.contains("새로운");
                })
                .map(n -> n.notificationText())
                .limit(3)
                .collect(Collectors.toList());
    }

    private String extractText(Map<?, ?> resp) {
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
            return sb.toString().replaceAll("^[\"\"''\\s]+|[\"\"''\\s]+$", "");
        } catch (Exception e) {
            return "";
        }
    }

    private String buildPrompt(UserData userData) {
        User user = userService.getUserbyUserId(userData.userId());
        
        // 목표 정보 포맷팅
        String goalInfo = userData.activeGoals().isEmpty() ? "활성 목표 없음" :
                userData.activeGoals().stream()
                        .map(ug -> ug.getGoal().getGoalName() + "(" + userData.goalAchievements().get(ug.getGoal().getId()) + "%%)")
                        .collect(Collectors.joining(", "));
        
        // 친구 활동 정보 포맷팅
        String friendActivities = userData.recentNotifications().stream()
                .limit(3)
                .map(n -> n.notificationText().replace("%", "%%"))
                .collect(Collectors.joining(", "));
        
        // 개선율 계산
        double improvementRate = userData.lastWeekAchievement() > 0 ? 
                ((double)(userData.thisWeekAchievement() - userData.lastWeekAchievement()) / userData.lastWeekAchievement()) * 100 : 0;

        return String.format("""
            시스템: 너는 따뜻하고 센스있는 카피라이터야.
            
            사용자 정보:
            - 이름: %s
            - 활성 목표: %s
            
            성취율 분석:
            - 이번주 성취율: %d%%
            - 지난주 성취율: %d%%
            - 개선율: %.1f%%
            - 연속 성공 일수: %d일
            - 최근 받은 댓글: %d개
            
            친구 활동:
            - 최근 친구 활동: %s
            - 친구 성취: %s
            
            메시지 생성 규칙:
            1. 연속 달성 축하: "최근 N일 연속으로 인증에 성공했네요 👏 오늘도 연속 기록을 이어가 봐요!"
            2. 댓글 언급: "어제 받은 응원 N개, 당신의 꾸준함을 다들 보고 있다는 증거예요 💪"
            3. 친구 챌린지 우승: "친구 __이 1:1 챌린지에서 우승했대요! __님도 이번 주 안에 새로운 1:1 챌린지에 도전해 보면 어때요?"
            4. 친구 새 목표: "__이 새로운 목표를 입력했어요! 👏 지금 페이스라면 나도 곧 도달할 수 있을 거예요"
            5. 개선율 높음: "이번 주 목표 달성량이 지난주에 비해서 N%%나 높아졌어요!"
            6. 일반 응원: "오늘 10분만 투자해도 오늘 하루가 달라질 거예요. 작은 습관이 큰 차이를 만들어요 ☀️"
            
            작성 스타일:
            - 이모지 사용: 1~2개
            - 한국어로만 작성
            - 최대 2문장
            - 80자 이내
            - 톤: 밝게
            - 비속어/혐오/민감 조언 금지
            - 반복 피하기
            - 성취율이 낮아도 진정성 있게 격려하고, 비꼬는 듯한 표현 금지
            
            출력: "[이름]님"으로 시작하는 존댓말 응원 메시지 한 개만 반환. 따옴표 없이 본문만.
            """,
                user.getNickname(),
                goalInfo,
                userData.thisWeekAchievement(),
                userData.lastWeekAchievement(),
                improvementRate,
                userData.consecutiveDays(),
                userData.recentCommentCount(),
                friendActivities.isEmpty() ? "최근 친구 활동 없음" : friendActivities,
                userData.friendAchievements().isEmpty() ? "친구 성취 없음" : String.join(", ", userData.friendAchievements())
            ).trim();
    }

    private String getDefaultMessage(UserData userData) {
        User user = userService.getUserbyUserId(userData.userId());
        
        String[] defaultMessages = {
            "%s님, %s 정말 열심히 하고 계시네요! 💪 꾸준히 노력하는 모습이 정말 대단해요! ✨",
            "%s님, %s 긍정적인 마음으로 차근차근 준비하면 분명 좋은 결과가 있을 거예요! 💪",
            "%s님, %s 목표를 향해 꾸준히 나아가는 모습이 정말 멋져요! 화이팅입니다! ✨",
            "%s님, %s 작은 진전도 큰 의미가 있어요! 계속해서 노력해주세요! 💪",
            "%s님, %s 어려운 시간이지만 당신의 노력을 응원합니다! 힘내세요! ✨",
            "%s님, 오늘 10분만 투자해도 오늘 하루가 달라질 거예요. 작은 습관이 큰 차이를 만들어요 ☀️"
        };
        
        String context = userData.activeGoals().isEmpty() ? "새로운 목표를 설정하는 중" : 
                        userData.activeGoals().get(0).getGoal().getGoalName() + " 목표 달성 중";
        
        int randomIndex = (int) (Math.random() * defaultMessages.length);
        return String.format(defaultMessages[randomIndex], user.getNickname(), context);
    }
}
