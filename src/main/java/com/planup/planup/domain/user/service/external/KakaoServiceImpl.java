package com.planup.planup.domain.user.service.external;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.AuthException;
import com.planup.planup.domain.user.dto.external.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoServiceImpl implements KaKaoService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public KakaoUserInfo getUserInfo(String accessToken) {
        String reqURL = "https://kapi.kakao.com/v2/user/me";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);
            headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<KakaoUserInfo> response = restTemplate.exchange(
                    reqURL,
                    HttpMethod.GET,
                    entity,
                    KakaoUserInfo.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("카카오 유저 정보 조회 실패: {}", e.getMessage());
            throw new AuthException(ErrorStatus.KAKAO_USER_INFO_FAILED);
        }
    }
}
