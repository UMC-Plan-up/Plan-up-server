package com.planup.planup.config;

import com.planup.planup.validation.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.frontend.urls}")
    private String frontendUrls;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // AuthenticationEntryPoint 설정
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            // 응답 본문 작성
                            response.getWriter().write(
                                    "{" +
                                            "\"isSuccess\":false," +
                                            "\"code\":\"UNAUTHORIZED\"," +
                                            "\"message\":\"인증이 필요합니다\"," +
                                            "\"result\":null" +
                                            "}"
                            );
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        //Swagger 및 정적 리소스
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-config",
                                "/swagger-resources/**",
                                "/api-docs/**",
                                "/webjars/**"
                        ).permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        // 인증 불필요 - 회원가입/로그인 관련
                        .requestMatchers("/users/signup", "/users/login").permitAll()
                        // 이메일 인증 (회원가입 시)
                        .requestMatchers("/users/email/send").permitAll()
                        .requestMatchers("/users/email/resend").permitAll()
                        .requestMatchers("/users/email/verify-link").permitAll()
                        .requestMatchers("/users/email/verification-status").permitAll()
                        .requestMatchers("/users/email/check-duplicate").permitAll()
                        .requestMatchers("/users/email/change-link").permitAll()  // 토큰 인증

                        .requestMatchers("/users/password/**").permitAll() // 비밀번호 재설정
                        .requestMatchers("/users/auth/**").permitAll()
                        .requestMatchers("/users/invite-code/validate").permitAll()  // 초대 코드 검증
                        .requestMatchers("/users/nickname/check-duplicate").permitAll()  // 닉네임 중복
                        // 인증 불필요 - 프로필 관련 (회원가입 시)
                        .requestMatchers(HttpMethod.POST, "/profile/image").permitAll()  // 회원가입 시 프로필 업로드
                        .requestMatchers(HttpMethod.GET, "/profile/nickname/random").permitAll()  // 랜덤 닉네임 생성
                        // 인증 불필요 - 약관 조회
                        .requestMatchers(HttpMethod.GET, "/terms/**").permitAll()
                        // 인증 필요 - 모든 기능
                        .requestMatchers("/users/**").authenticated()           // 유저 관련
                        .requestMatchers("/mypage/**").authenticated()          // 마이페이지
                        .requestMatchers("/profile/**").authenticated()         // 프로필
                        .requestMatchers("/goals/**").authenticated()           // 목표 관리
                        .requestMatchers("/community/**").authenticated()       // 커뮤니티 (UserGoalController)
                        .requestMatchers("/challenges/**").authenticated()      // 챌린지
                        .requestMatchers("/friends/**").authenticated()         // 친구
                        .requestMatchers("/verification/**").authenticated()    // 인증
                        .requestMatchers("/report/**").authenticated()          // 리포트
                        .requestMatchers("/notifications/**").authenticated()   // 알림
                        .requestMatchers("/api/encourage/**").authenticated()   // 격려 메시지
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 프론트엔드 URL 허용 (쉼표로 구분된 여러 URL 지원)
        configuration.setAllowedOrigins(Arrays.asList(frontendUrls.split(",")));

        // 모든 HTTP 메소드 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 모든 헤더 허용
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 인증 정보 허용 (쿠키, Authorization 헤더 등)
        configuration.setAllowCredentials(true);

        // Preflight 요청 캐시 시간 (1시간)
        configuration.setMaxAge(3600L);

        // 노출할 헤더 설정
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}