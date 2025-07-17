package com.planup.planup.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
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
                                "/",
                                "/swagger-ui.html",
                                "/api-docs",

                                "/swagger-resources/**",
                                "/webjars/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().permitAll() // << 여기를 바꿔야 인증 없이 접근됨
                );

        return http.build();
    }
}
