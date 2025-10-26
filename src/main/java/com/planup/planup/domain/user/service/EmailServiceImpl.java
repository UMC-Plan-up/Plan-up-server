package com.planup.planup.domain.user.service;

import com.planup.planup.domain.user.dto.EmailVerifyLinkResponseDTO;
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

        try {
            redisTemplate.opsForValue().set(
                    "email-verification:" + verificationToken,
                    email,
                    30,
                    TimeUnit.MINUTES
            );
        } catch (Exception e) {
            log.error("Redis 토큰 저장 실패: {}", e.getMessage());
            throw new RuntimeException("이메일 인증 토큰 저장에 실패했습니다.");
        }

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
            log.error("토큰 검증 실패: {}", e.getMessage());
            throw new IllegalArgumentException("토큰이 만료되었거나 이미 사용되었습니다.");
        }

        if (isEmailVerified(email)) {
            // 이미 인증된 경우: 토큰은 TTL까지 유지 (삭제하지 않음)
            return email;
        }
        
        try {
            // 이메일 인증 완료 표시
            redisTemplate.opsForValue().set(
                    "email-verified:" + email,
                    "VERIFIED",
                    60,
                    TimeUnit.MINUTES
            );
            
            // 사용된 토큰은 TTL까지 유지 (삭제하지 않음)
            
        } catch (Exception e) {
            log.error("Redis 저장/삭제 실패: {}", e.getMessage());
            throw new RuntimeException("이메일 인증 처리 중 오류가 발생했습니다.");
        }
        
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
    public String resendPasswordChangeEmail(String email, Boolean isLoggedIn) {
        clearExistingPasswordChangeTokens(email);
        return sendPasswordChangeEmail(email, isLoggedIn);  // 실제 로그인 상태 전달
    }

    @Override
    public String sendPasswordChangeEmail(String email, Boolean isLoggedIn) {
        String token = UUID.randomUUID().toString();
    
        // Redis에 토큰 저장 (기존 이메일 변경 방식과 동일)
        // "email:isLoggedIn" 형태로 저장
        redisTemplate.opsForValue().set(
            "password-change:" + token,
            email + ":" + isLoggedIn,
            30,
            TimeUnit.MINUTES
        );
        
        // 이메일 발송 (기존 방식과 동일)
        String changeUrl = appDomain + "/users/password/change-link?token=" + token;
        sendPasswordChangeEmailContent(email, changeUrl);
        
        return token;
    }

    private void clearExistingPasswordChangeTokens(String email) {
        Set<String> keys = redisTemplate.keys("password-change:*");
        if (keys != null) {
            for (String key : keys) {
                String storedEmail = redisTemplate.opsForValue().get(key);
                if (email.equals(storedEmail)) {
                    redisTemplate.delete(key);
                }
            }
        }
    }

    @Override
    public String[] validatePasswordChangeToken(String token) {
        String value = redisTemplate.opsForValue().get("password-change:" + token);

        if (value == null) {
            throw new IllegalArgumentException("만료되거나 유효하지 않은 비밀번호 변경 토큰입니다.");
        }

        // "email:isLoggedIn" 형태로 분리
        String[] parts = value.split(":");
        String email = parts[0];
        String isLoggedIn = parts[1];
        
        return new String[]{email, isLoggedIn};
    }
    
    @Override
    public Boolean isPasswordChangeEmailVerified(String email) {
        // Redis에서 비밀번호 변경 이메일 인증 완료 여부 확인
        String verified = redisTemplate.opsForValue().get("password-change-verified:" + email);
        return "VERIFIED".equals(verified);
    }
    
    @Override
    public void markPasswordChangeEmailAsVerified(String email) {
        // Redis에 비밀번호 변경 이메일 인증 완료 표시 (24시간 유효)
        redisTemplate.opsForValue().set(
            "password-change-verified:" + email,
            "VERIFIED",
            24,
            TimeUnit.HOURS
        );
    }

    @Override
    public void clearVerificationToken(String email) {
        redisTemplate.delete("email-verified:" + email);
        clearExistingTokens(email);
    }

    private String getEmailByToken(String token) {
        try {
            String email = redisTemplate.opsForValue().get("email-verification:" + token);

            if (email == null) {
                throw new IllegalArgumentException("만료되거나 유효하지 않은 토큰입니다.");
            }
            return email;
        } catch (Exception e) {
            log.error("Redis 조회 중 오류 발생: {}", e.getMessage());
            throw new IllegalArgumentException("토큰 검증 중 오류가 발생했습니다.");
        }
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
                              font-weight: bold;"
                        target="_blank">
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
    private void sendPasswordChangeEmailContent(String email, String changeUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Plan-Up 비밀번호 변경 확인");
            helper.setText(createPasswordChangeEmailContent(changeUrl), true);
            helper.setFrom("noreply@planup.com");

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("비밀번호 변경 이메일 발송 실패", e);
        }
    }

    private String createPasswordChangeEmailContent(String changeUrl) {
        return """
            <div style="max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif;">
                <div style="text-align: center; margin-bottom: 30px;">
                    <h1 style="color: #4285f4;">Plan-Up</h1>
                </div>
                
                <h2 style="color: #333;">비밀번호 변경 확인</h2>
                <p style="color: #666; line-height: 1.6;">
                    안녕하세요!<br>
                    비밀번호 변경을 위해 아래 버튼을 클릭해주세요.
                </p>
                
                <div style="text-align: center; margin: 40px 0;">
                    <a href="%s" 
                       style="background: #4285f4; color: white; padding: 15px 30px; 
                              text-decoration: none; border-radius: 8px; display: inline-block;
                              font-weight: bold;"
                         target="_blank">
                        비밀번호 변경하기
                    </a>
                </div>
                
                <p style="color: #999; font-size: 14px;">
                    * 이 링크는 30분 후 만료됩니다.<br>
                    * 본인이 요청하지 않은 경우 이 이메일을 무시해주세요.
                </p>
            </div>
            """.formatted(changeUrl);
    }

    @Override
    public String createSuccessHtml(String email, String deepLinkUrl) {
        return """
        <!DOCTYPE html>
        <html lang="ko">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Plan-Up 이메일 인증</title>
            <style>
                body {
                    margin: 0;
                    padding: 20px;
                    font-family: Arial, sans-serif;
                    background-color: #f5f5f5;
                }
                .container {
                    max-width: 600px;
                    margin: 0 auto;
                    padding: 20px;
                    background: white;
                    border-radius: 8px;
                    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                }
                .header {
                    text-align: center;
                    margin-bottom: 30px;
                }
                .header h1 {
                    color: #4285f4;
                    font-size: 2rem;
                    margin: 0;
                }
                .content {
                    text-align: center;
                }
                h2 {
                    color: #333;
                    margin-bottom: 20px;
                    font-size: 1.5rem;
                }
                p {
                    color: #666;
                    line-height: 1.6;
                    margin-bottom: 20px;
                    font-size: 1rem;
                }
                .footer {
                    margin-top: 40px;
                    padding-top: 20px;
                    border-top: 1px solid #eee;
                }
                .footer p {
                    color: #999;
                    font-size: 14px;
                    margin: 5px 0;
                }
            </style>
            <script>
                // 3초 후 자동으로 앱으로 리다이렉트
                console.log("딥링크 URL:", "%s");
                setTimeout(function() {
                    console.log("딥링크 실행 시도...");
                    window.location.href = "%s";
                }, 3000);
            </script>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Plan-Up</h1>
                </div>
                <div class="content">
                    <h2>이메일 인증 완료</h2>
                    <p>
                        인증이 성공하였습니다!<br>
                        잠시 후 Plan-Up 앱으로 자동 이동합니다.
                    </p>
                </div>
                <div class="footer">
                    <p>* 자동으로 앱이 열리지 않는다면 Plan-Up 앱을 직접 실행해주세요.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(deepLinkUrl, deepLinkUrl);
    }

    @Override
    public String createFailureHtml() {
        return """
        <!DOCTYPE html>
        <html lang="ko">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Plan-Up 이메일 인증</title>
            <style>
                body {
                    margin: 0;
                    padding: 20px;
                    font-family: Arial, sans-serif;
                    background-color: #f5f5f5;
                }
                .container {
                    max-width: 600px;
                    margin: 0 auto;
                    padding: 20px;
                    background: white;
                    border-radius: 8px;
                    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                }
                .header {
                    text-align: center;
                    margin-bottom: 30px;
                }
                .header h1 {
                    color: #4285f4;
                    font-size: 2rem;
                    margin: 0;
                }
                .content {
                    text-align: center;
                }
                h2 {
                    color: #333;
                    margin-bottom: 20px;
                    font-size: 1.5rem;
                }
                p {
                    color: #666;
                    line-height: 1.6;
                    margin-bottom: 20px;
                    font-size: 1rem;
                }
                .footer {
                    margin-top: 40px;
                    padding-top: 20px;
                    border-top: 1px solid #eee;
                }
                .footer p {
                    color: #999;
                    font-size: 14px;
                    margin: 5px 0;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Plan-Up</h1>
                </div>
                <div class="content">
                    <h2>이메일 인증 실패</h2>
                    <p>
                        인증에 실패하였습니다.<br>
                        Plan-Up 앱에서 다시 시도해주세요.
                    </p>
                </div>
                <div class="footer">
                    <p>* 링크가 만료되었거나 올바르지 않습니다.</p>
                    <p>* 앱에서 인증 메일을 다시 요청해주세요.</p>
                </div>
            </div>
        </body>
        </html>
        """;
    }



    @Override
    public String sendEmailChangeVerificationEmail(String currentEmail, String newEmail) {
        String changeToken = UUID.randomUUID().toString();
        
        // Redis에 토큰 저장 (기존:새 이메일 형태)
        redisTemplate.opsForValue().set(
            "email-change:" + changeToken,
            currentEmail + ":" + newEmail,
            30,
            TimeUnit.MINUTES
        );
        
        String changeUrl = appDomain + "/users/email/change-link?token=" + changeToken;
        sendEmailChangeVerificationEmailContent(currentEmail, newEmail, changeUrl);
        
        return changeToken;
    }

    @Override
    public String validateEmailChangeToken(String token) {
        String emailPair = redisTemplate.opsForValue().get("email-change:" + token);
        
        if (emailPair == null) {
            throw new IllegalArgumentException("만료되거나 유효하지 않은 이메일 변경 토큰입니다.");
        }
        
        // 토큰은 이메일 변경 완료 후 삭제
        
        return emailPair; // "currentEmail:newEmail" 형태로 반환
    }

    private void sendEmailChangeVerificationEmailContent(String currentEmail, String newEmail, String changeUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
    
            helper.setTo(newEmail);
            helper.setSubject("Plan-Up 이메일 변경 확인");
            helper.setText(createEmailChangeVerificationContent(currentEmail, newEmail, changeUrl), true);
            helper.setFrom("noreply@planup.com");
    
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("이메일 변경 인증 메일 발송 실패", e);
        }
    }
    private String createEmailChangeVerificationContent(String currentEmail, String newEmail, String changeUrl) {
        return """
            <div style="max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif;">
                <div style="text-align: center; margin-bottom: 30px;">
                    <h1 style="color: #4285f4;">Plan-Up</h1>
                </div>
                
                <h2 style="color: #333;">이메일 변경 확인</h2>
                <p style="color: #666; line-height: 1.6;">
                    안녕하세요!<br>
                    현재 이메일: <strong>%s</strong><br>
                    변경할 이메일: <strong>%s</strong><br>
                    이메일 변경을 위해 아래 버튼을 클릭해주세요.
                </p>
                
                <div style="text-align: center; margin: 40px 0;">
                    <a href="%s" 
                       style="background: #4285f4; color: white; padding: 15px 30px; 
                              text-decoration: none; border-radius: 8px; display: inline-block;
                              font-weight: bold;"
                        target="_blank">
                        이메일 변경 확인
                    </a>
                </div>
                
                <p style="color: #999; font-size: 14px;">
                    * 이 링크는 30분 후 만료됩니다.<br>
                    * 본인이 요청하지 않은 경우 이 이메일을 무시해주세요.
                </p>
            </div>
            """.formatted(currentEmail, newEmail, changeUrl);
    }
    
    @Override
    public EmailVerifyLinkResponseDTO handleEmailChangeLink(String token) {
        try {
            String emailPair = validateEmailChangeToken(token);
            String[] emails = emailPair.split(":");
            String currentEmail = emails[0];
            String newEmail = emails[1];
            
            String deepLinkUrl = "planup://email/change?currentEmail=" +
                    java.net.URLEncoder.encode(currentEmail, java.nio.charset.StandardCharsets.UTF_8) +
                    "&newEmail=" + java.net.URLEncoder.encode(newEmail, java.nio.charset.StandardCharsets.UTF_8) +
                    "&verified=true&token=" + token +
                    "&from=email_change";

            return EmailVerifyLinkResponseDTO.builder()
                    .verified(true)
                    .message("이메일 변경 요청이 확인되었습니다")
                    .deepLinkUrl(deepLinkUrl)
                    .email(currentEmail + ":" + newEmail)  // 기존:새 이메일 형태로 저장
                    .build();
                    
        } catch (IllegalArgumentException e) {
            return EmailVerifyLinkResponseDTO.builder()
                    .verified(false)
                    .message("이메일 변경 요청 확인에 실패했습니다")
                    .build();
        }
    }

    @Override
    public String resendEmailChangeVerificationEmail(String currentEmail, String newEmail) {
        // 기존 이메일 변경 토큰들 정리
        clearExistingEmailChangeTokens(currentEmail, newEmail);
        
        // 새로운 토큰으로 재발송
        return sendEmailChangeVerificationEmail(currentEmail, newEmail);
    }

    private void clearExistingEmailChangeTokens(String currentEmail, String newEmail) {
        Set<String> keys = redisTemplate.keys("email-change:*");
        if (keys != null) {
            for (String key : keys) {
                String emailPair = redisTemplate.opsForValue().get(key);
                if (emailPair != null && emailPair.contains(currentEmail) && emailPair.contains(newEmail)) {
                    redisTemplate.delete(key);
                }
            }
        }
    }
    
    @Override
    public void clearEmailChangeToken(String token) {
        redisTemplate.delete("email-change:" + token);
    }

    @Override
    public void clearPasswordChangeToken(String email) {
        // 해당 이메일의 비밀번호 변경 관련 토큰들 정리
        redisTemplate.delete("password-change-verified:" + email);
        
        // 기존 비밀번호 변경 토큰들도 정리
        Set<String> keys = redisTemplate.keys("password-change:*");
        if (keys != null) {
            for (String key : keys) {
                String storedEmail = redisTemplate.opsForValue().get(key);
                if (email.equals(storedEmail)) {
                    redisTemplate.delete(key);
                }
            }
        }
    }
}