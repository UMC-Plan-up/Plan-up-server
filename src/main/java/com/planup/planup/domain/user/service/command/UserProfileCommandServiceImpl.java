package com.planup.planup.domain.user.service.command;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.AuthException;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.global.service.ImageUploadService;
import com.planup.planup.domain.user.converter.UserProfileConverter;
import com.planup.planup.domain.user.dto.AuthResponseDTO;
import com.planup.planup.domain.user.dto.FileResponseDTO;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.enums.UserActivate;
import com.planup.planup.domain.user.repository.UserRepository;
import com.planup.planup.domain.user.service.query.UserQueryService;
import com.planup.planup.domain.user.service.util.EmailTemplateUtil;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserProfileCommandServiceImpl implements UserProfileCommandService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;
    private final ImageUploadService imageUploadService;
    private final UserQueryService userQueryService;
    private final UserProfileConverter userProfileConverter;

    @Qualifier("objectRedisTemplate")
    private final RedisTemplate<String, Object> objectRedisTemplate;

    @Value("${app.domain:http://localhost:8080}")
    private String appDomain;

    private static final String TEMP_PROFILE_PREFIX = "temp_profile:";

    // ========== 기본 정보 수정 ==========

    @Override
    public String updateNickname(Long userId, String nickname) {

        // 입력값 검증
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new UserException(ErrorStatus.INVALID_NICKNAME);
        }
        User user = userQueryService.getUserByUserId(userId);

        if (user.getNickname().equals(nickname)) {
            return nickname;
        }

        if (userRepository.existsByNickname(nickname)) {
            throw new UserException(ErrorStatus.EXIST_NICKNAME);
        }

        user.setNickname(nickname);
        return user.getNickname();
    }

    @Override
    public boolean updateMarketingNotificationAllow(Long userId) {
        User user = userQueryService.getUserByUserId(userId);
        return user.updateMarketingNotificationAllow();
    }

    @Override
    public boolean updateServiceNotificationAllow(Long userId) {
        User user = userQueryService.getUserByUserId(userId);
        return user.updateServiceNotificationAllow();
    }

    @Override
    public String updateEmail(Long userId, String newEmail) {

        if (newEmail == null || newEmail.trim().isEmpty()) {
            throw new UserException(ErrorStatus.INVALID_EMAIL_FORMAT);
        }

        User user = userQueryService.getUserByUserId(userId);

        if (user.getEmail().equals(newEmail)) {
            return newEmail;
        }

        if (userRepository.existsByEmailAndUserActivate(newEmail, UserActivate.ACTIVE)) {
            throw new UserException(ErrorStatus.USER_EMAIL_ALREADY_EXISTS);
        }
        // user.setEmailVerified(false); // 이메일 변경되었으므로 인증 상태 초기화

        user.setEmail(newEmail);
        return user.getEmail();
    }

    // ========== 프로필 이미지 ==========

    @Override
    public FileResponseDTO.ImageUpload uploadProfileImage(MultipartFile file, String email) {
        String imageUrl = imageUploadService.uploadImage(file, "profile");

        String redisKey = TEMP_PROFILE_PREFIX + email;
        try {
            // Redis 저장 실패 시 AuthException 발생
            objectRedisTemplate.opsForValue().set(redisKey, imageUrl, Duration.ofHours(1));
        } catch (Exception e) {
            log.error("Redis 임시 이미지 URL 저장 실패: {}", e.getMessage(), e);
            // 이미지는 저장되었지만, 후속 작업에 문제가 생길 수 있으므로 예외 처리
            throw new AuthException(ErrorStatus.REDIS_SAVE_FAILED);
        }
        return userProfileConverter.toImageUploadResponseDTO(imageUrl);
    }

    @Override
    public FileResponseDTO.ImageUpload updateProfileImage(Long userId, MultipartFile file) {
        User user = userQueryService.getUserByUserId(userId);

        if (user.getProfileImg() != null && !user.getProfileImg().trim().isEmpty()) {
            try {
                imageUploadService.deleteImage(user.getProfileImg());
            } catch (Exception e) {
                // 이미지 삭제는 실패해도 DB 업데이트는 진행해야 하므로 경고 로그만 남김
                log.warn("기존 프로필 이미지 삭제 실패 (무시): URL={}, Error={}", user.getProfileImg(), e.getMessage());
            }
        }
        String newImageUrl = imageUploadService.uploadImage(file, "profile");

        user.updateProfileImage(newImageUrl);

        return FileResponseDTO.ImageUpload.builder()
                .imageUrl(newImageUrl)
                .build();
    }

    // ========== 이메일 변경 ==========

    @Override
    public AuthResponseDTO.EmailSend sendEmailChangeVerification(Long userId, String newEmail) {
        User currentUser = userQueryService.getUserByUserId(userId);
        return sendEmailChangeVerification(currentUser.getEmail(), newEmail);
    }

    @Override
    public AuthResponseDTO.EmailSend sendEmailChangeVerification(String currentEmail, String newEmail) {
        userQueryService.checkEmail(newEmail);

        String changeToken = UUID.randomUUID().toString();

        try {
            redisTemplate.opsForValue().set(
                    "email-change:" + changeToken,
                    currentEmail + ":" + newEmail,
                    30,
                    TimeUnit.MINUTES
            );
        } catch (Exception e) {
            log.error("Redis 이메일 변경 토큰 저장 실패: {}", e.getMessage(), e);
            throw new AuthException(ErrorStatus.REDIS_SAVE_FAILED);
        }

        String changeUrl = appDomain + "/users/email/change-link?token=" + changeToken;
        try {
            sendEmailChangeVerificationEmailContent(currentEmail, newEmail, changeUrl);
        } catch (Exception e) {
            log.error("이메일 변경 인증 메일 발송 실패: {}", e.getMessage(), e);
            redisTemplate.delete("email-change:" + changeToken); // 토큰 정리
            throw new AuthException(ErrorStatus.EMAIL_SEND_FAILED);
        }

        return userProfileConverter.toEmailSendResponseDTO(newEmail, changeToken, "이메일 변경 인증 메일이 발송되었습니다.");
    }

    @Override
    public AuthResponseDTO.EmailSend resendEmailChangeVerification(Long userId, String newEmail) {
        User currentUser = userQueryService.getUserByUserId(userId);
        return resendEmailChangeVerification(currentUser.getEmail(), newEmail);
    }

    @Override
    public AuthResponseDTO.EmailSend resendEmailChangeVerification(String currentEmail, String newEmail) {
        userQueryService.checkEmail(newEmail);
        return sendEmailChangeVerification(currentEmail, newEmail);
    }

    @Override
    public void completeEmailChange(String token) {
        String emailPair = validateEmailChangeToken(token);
        String[] emails = emailPair.split(":");
        String currentEmail = emails[0];
        String newEmail = emails[1];

        User user = userRepository.findByEmailAndUserActivate(currentEmail, UserActivate.ACTIVE)
                .orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_USER));

        user.setEmail(newEmail);
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());

        redisTemplate.delete("email-change:" + token); // 사용된 이메일 변경 토큰 삭제
        redisTemplate.delete("email-verified:" + currentEmail); // 기존 이메일의 '인증 완료 마킹' 상태 삭제
    }

    @Override
    public String handleEmailChangeLink(String token) {
        try {
            // 토큰 검증 및 이메일 추출
            String emailPair = validateEmailChangeToken(token);
            String[] emails = emailPair.split(":");
            String currentEmail = emails[0];
            String newEmail = emails[1];

            completeEmailChange(token);


            String encodedEmails = java.net.URLEncoder.encode(currentEmail + ":" + newEmail, java.nio.charset.StandardCharsets.UTF_8.toString());
            String deepLinkUrl = "planup://email/change/complete?verified=true&token=" + token + "&emails=" + encodedEmails;

            return EmailTemplateUtil.createSuccessHtml(currentEmail + ":" + newEmail, deepLinkUrl);

        } catch (IllegalArgumentException | UnsupportedEncodingException e) {
            log.error("이메일 변경 링크 처리 실패: {}", e.getMessage(), e);
            return EmailTemplateUtil.createFailureHtml();
        }
    }

    // ========== 토큰 검증 ==========

    @Override
    public String validateToken(String token) {
        return getEmailByToken(token);
    }

    @Override
    public String[] validatePasswordChangeToken(String token) {
        String value = redisTemplate.opsForValue().get("password-change:" + token);

        if (value == null) {
            throw new AuthException(ErrorStatus.PASSWORD_TOKEN_INVALID);
        }

        String[] parts = value.split(":");
        if (parts.length < 2) { // 데이터 무결성 체크 추가
            throw new AuthException(ErrorStatus.PASSWORD_TOKEN_INVALID);
        }
        return parts;
    }

    @Override
    public String validateEmailChangeToken(String token) {
        String emailPair = redisTemplate.opsForValue().get("email-change:" + token);

        if (emailPair == null) {
            throw new AuthException(ErrorStatus.PASSWORD_TOKEN_INVALID);
        }

        if (!emailPair.contains(":")) {
            throw new AuthException(ErrorStatus.PASSWORD_TOKEN_INVALID);
        }

        return emailPair;
    }

    // ========== 인증 완료 ==========

    @Override
    public String completeVerification(String verificationToken) {
        String email = getEmailByToken(verificationToken);

        if (userQueryService.isEmailVerified(email)) {
            return email;
        }
        // Redis에 인증 마킹 저장
        try {
            redisTemplate.opsForValue().set(
                    "email-verified:" + email,
                    "VERIFIED",
                    60,
                    TimeUnit.MINUTES
            );
        } catch (Exception e) {
            log.error("Redis 저장 실패: {}", e.getMessage(), e);
            throw new AuthException(ErrorStatus.REDIS_SAVE_FAILED);
        }

        // 사용 완료된 토큰 삭제
        redisTemplate.delete("email-verification:" + verificationToken);

        return email;
    }

    // ========== 토큰 정리 ==========

    @Override
    public void clearVerificationToken(String email) {
        redisTemplate.delete("email-verified:" + email);
    }

    @Override
    public void clearPasswordChangeToken(String email) {
        redisTemplate.delete("password-change-verified:" + email);

        Set<String> keys = redisTemplate.keys("password-change:*");
        if (keys != null) {
            for (String key : keys) {
                String storedValue = redisTemplate.opsForValue().get(key);
                if (storedValue != null && storedValue.startsWith(email + ":")) {
                    redisTemplate.delete(key);
                }
            }
        }
    }

    @Override
    public void clearEmailChangeToken(String token) {
        redisTemplate.delete("email-change:" + token);
    }

    // ========== Private 헬퍼 메서드 ==========

    private String getEmailByToken(String token) {
        String email = redisTemplate.opsForValue().get("email-verification:" + token);

        if (email == null) {
            throw new AuthException(ErrorStatus.INVALID_EMAIL_TOKEN);
        }
        return email;
    }

    private void clearExistingTokens(String email) {
        if (userQueryService.isEmailVerified(email)) {
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
            throw new AuthException(ErrorStatus.EMAIL_SEND_FAILED);
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
}