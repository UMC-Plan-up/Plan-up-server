package com.planup.planup.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", // 홈도 열어두고 싶다면 추가
                                "/api/v1/users/login", "/api/v1/users", "/api/v1/users/duplicate", "/api/v1/users/check-nickname",
                                "/swagger-ui/**", "/api-docs/**", "/api/v1/oauth2/**", "/login/oauth2/**", "/oauth2/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") // 커스텀 로그인 페이지를 쓰지 않으면 생략해도 됨
                        .permitAll()
                );

        return http.build();
    }
}
