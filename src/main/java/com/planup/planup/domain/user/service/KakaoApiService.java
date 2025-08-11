package com.planup.planup.domain.user.service;


import com.planup.planup.domain.user.dto.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoApiService {

    private final WebClient webClient = WebClient.builder().build();

    @Value("${kakao.client-id}")
    private String clientId;

    // 인가코드로 카카오 사용자 정보 바로 가져오기
    public KakaoUserInfo getUserInfo(String code) {
        try {
            // 인가코드 → 액세스토큰
            String accessToken = getAccessToken(code);

            // 액세스토큰 → 사용자 정보
            return getUserInfoByToken(accessToken);

        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 실패", e);
            throw new RuntimeException("카카오 로그인 실패");
        }
    }

    // 인가코드 → 액세스토큰
    private String getAccessToken(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", "http://localhost:8080/test/kakao/callback");
        params.add("code", code);

        String response = webClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // 간단한 파싱 (JSON에서 access_token 추출)
        return extractAccessToken(response);
    }

    // 액세스토큰 → 사용자 정보
    private KakaoUserInfo getUserInfoByToken(String accessToken) {
        return webClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserInfo.class)
                .block();
    }

    // JSON 응답에서 access_token 추출 (간단 파싱)
    private String extractAccessToken(String response) {
        String tokenPrefix = "\"access_token\":\"";
        int startIndex = response.indexOf(tokenPrefix) + tokenPrefix.length();
        int endIndex = response.indexOf("\"", startIndex);
        return response.substring(startIndex, endIndex);
    }
}