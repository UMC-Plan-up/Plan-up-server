package com.planup.planup.domain.user.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.domain:http://localhost:8080}")
    private String appDomain;

    @Override
    public void sendVerificationLink(String email) {
        // 토큰 생성
        String token = UUID.randomUUID().toString();

        // Redis에 30분간 저장
        redisTemplate.opsForValue().set(
                "verify-token:" + token,
                email,
                30,
                TimeUnit.MINUTES
        );

        // 인증 링크 생성
        String verificationUrl = appDomain + "/users/email/verify?token=" + token;

        // 이메일 발송
        sendVerificationEmail(email, verificationUrl);
    }

    @Override
    public String verifyToken(String token) {
        String email = redisTemplate.opsForValue().get("verify-token:" + token);

        if (email == null) {
            throw new IllegalArgumentException("만료되거나 유효하지 않은 토큰입니다.");
        }

        // 토큰 삭제
        redisTemplate.delete("verify-token:" + token);

        // 인증 완료 상태를 Redis에 저장 (30분 유지)
        redisTemplate.opsForValue().set(
                "email-verified:" + email,
                "VERIFIED",
                30,
                TimeUnit.MINUTES
        );

        return email;
    }

    @Override
    public boolean isEmailVerified(String email) {
        String verified = redisTemplate.opsForValue().get("email-verified:" + email);
        return "VERIFIED".equals(verified);
    }

    @Override
    public void clearVerificationToken(String email) {
        redisTemplate.delete("email-verified:" + email);
    }

    @Override
    public void resendVerificationLink(String email) {
        // 기존 토큰들 삭제 (선택사항)
        // 그냥 새로 발송해도 됨
        sendVerificationLink(email);
    }


    // 실제 이메일 발송
    private void sendVerificationEmail(String to, String verificationUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Plan-Up 이메일 인증");
            helper.setText(createEmailContent(verificationUrl), true);
            helper.setFrom("noreply@planup.com");

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("이메일 발송 실패", e);
        }
    }

    private String createEmailContent(String verificationUrl) {
        return """
            <div style="max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif;">
                <div style="text-align: center; margin-bottom: 30px;">
                    <h1 style="color: #4285f4;">Plan-Up</h1>
                </div>
                
                <h2 style="color: #333;">이메일 인증</h2>
                <p style="color: #666; line-height: 1.6;">
                    안녕하세요!<br>
                    Plan-Up 회원가입을 완료하려면 아래 버튼을 클릭해주세요.
                </p>
                
                <div style="text-align: center; margin: 40px 0;">
                    <a href="%s" 
                       style="background: #4285f4; color: white; padding: 15px 30px; 
                              text-decoration: none; border-radius: 8px; display: inline-block;
                              font-weight: bold;">
                        이메일 인증하기
                    </a>
                </div>
                
                <p style="color: #999; font-size: 14px;">
                    * 이 링크는 30분 후 만료됩니다.<br>
                    * 본인이 요청하지 않은 경우 이 이메일을 무시해주세요.
                </p>
            </div>
            """.formatted(verificationUrl);
    }
}