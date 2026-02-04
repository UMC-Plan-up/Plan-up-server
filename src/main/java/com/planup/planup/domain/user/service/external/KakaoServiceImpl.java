package com.planup.planup.domain.user.service.external;

import com.planup.planup.domain.user.dto.external.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoServiceImpl implements KaKaoService {

    // 이메일을 받아서 KakaoUserInfo 객체로 감싸서 반환 (Mocking)
    @Override
    public KakaoUserInfo getUserInfo(String email) {
        KakaoUserInfo userInfo = new KakaoUserInfo();

        // 중복 방지용 랜덤 ID
        userInfo.setId(System.nanoTime());

        KakaoUserInfo.KakaoAccount account = new KakaoUserInfo.KakaoAccount();
        account.setEmail(email);

        // 카카오 API는 보통 "male" 또는 "female" 소문자 문자열 반환
        // Converter가 이를 보고 Gender.MALE로 변환
        account.setGender("male");

        userInfo.setKakaoAccount(account);
        
        return userInfo;
    }
}
