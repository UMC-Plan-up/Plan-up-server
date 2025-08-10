package com.planup.planup.domain.user.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.domain:http://localhost:8080}")
    private String appDomain;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public String sendVerificationEmail(String email) {

        String verificationToken = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(
                "email-verification:" + verificationToken,
                email,
                30,
                TimeUnit.MINUTES
        );

        String verificationUrl = appDomain + "/users/email/verify-link?token=" + verificationToken;
        sendEmail(email, verificationUrl);

        return verificationToken;
    }

    @Override
    public String validateToken(String token) {
        return getEmailByToken(token);
    }

    @Override
    public String completeVerification(String verificationToken) {
        String email;
        try {
            email = getEmailByToken(verificationToken);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("토큰이 만료되었거나 이미 사용되었습니다.");
        }

        if (isEmailVerified(email)) {
            log.info("이미 인증된 이메일: {}", email);
            return email;
        }
        redisTemplate.opsForValue().set(
                "email-verified:" + email,
                "VERIFIED",
                60,
                TimeUnit.MINUTES
        );
        log.info("이메일 인증 완료: {}", email);
        return email;
    }


    @Override
    public boolean isEmailVerified(String email) {
        String verified = redisTemplate.opsForValue().get("email-verified:" + email);
        return "VERIFIED".equals(verified);
    }

    @Override
    public String resendVerificationEmail(String email) {
        clearExistingTokens(email);
        return sendVerificationEmail(email);
    }

    @Override
    public void clearVerificationToken(String email) {
        redisTemplate.delete("email-verified:" + email);
        clearExistingTokens(email);
    }

    private String getEmailByToken(String token) {
        String email = redisTemplate.opsForValue().get("email-verification:" + token);

        if (email == null) {
            throw new IllegalArgumentException("만료되거나 유효하지 않은 토큰입니다.");
        }
        return email;
    }

    private void clearExistingTokens(String email) {
        if (isEmailVerified(email)) {
            return;
        }
        Set<String> keys = redisTemplate.keys("email-verification:*");
        if (keys != null) {
            for (String key : keys) {
                String storedEmail = redisTemplate.opsForValue().get(key);
                if (email.equals(storedEmail)) {
                    redisTemplate.delete(key);
                }
            }
        }
    }

    // 실제 이메일 발송
    private void sendEmail(String to, String verificationUrl) {
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