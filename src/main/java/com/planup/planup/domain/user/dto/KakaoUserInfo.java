package com.planup.planup.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoUserInfo {

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {
        private String email;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {
        private String nickname;
    }

    public String getEmail() {
        return kakaoAccount != null ? kakaoAccount.getEmail() : null;
    }

    public String getNickname() {
        return properties != null ? properties.getNickname() : null;
    }
}
