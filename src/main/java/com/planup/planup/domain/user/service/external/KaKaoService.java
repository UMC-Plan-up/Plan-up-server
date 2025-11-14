package com.planup.planup.domain.user.service.external;

import com.planup.planup.domain.user.dto.external.KakaoUserInfo;

public interface KaKaoService {

    KakaoUserInfo getUserInfo(String code);
}
