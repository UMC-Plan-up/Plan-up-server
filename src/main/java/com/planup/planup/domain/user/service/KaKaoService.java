package com.planup.planup.domain.user.service;

import com.planup.planup.domain.user.dto.KakaoUserInfo;

public interface KaKaoService {

    KakaoUserInfo getUserInfo(String code);


}
