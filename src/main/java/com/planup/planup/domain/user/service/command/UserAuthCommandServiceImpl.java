package com.planup.planup.domain.user.service.command;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.friend.entity.FriendStatus;
import com.planup.planup.domain.friend.repository.FriendRepository;
import com.planup.planup.domain.oauth.entity.AuthProvideerEnum;
import com.planup.planup.domain.oauth.entity.OAuthAccount;
import com.planup.planup.domain.oauth.repository.OAuthAccountRepository;
import com.planup.planup.domain.user.converter.UserAuthConverter;
import com.planup.planup.domain.user.dto.*;
import com.planup.planup.domain.user.dto.external.KakaoUserInfo;
import com.planup.planup.domain.user.entity.Terms;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.entity.UserTerms;
import com.planup.planup.domain.user.entity.UserWithdrawal;
import com.planup.planup.domain.user.enums.Role;
import com.planup.planup.domain.user.enums.UserActivate;
import com.planup.planup.domain.user.enums.UserLevel;
import com.planup.planup.domain.user.repository.TermsRepository;
import com.planup.planup.domain.user.repository.UserRepository;
import com.planup.planup.domain.user.repository.UserTermsRepository;
import com.planup.planup.domain.user.repository.UserWithdrawalRepository;
import com.planup.planup.domain.user.service.external.KaKaoService;
import com.planup.planup.domain.user.service.query.UserQueryService;
import com.planup.planup.domain.user.service.util.EmailTemplateUtil;
import com.planup.planup.validation.jwt.JwtUtil;
import com.planup.planup.validation.jwt.dto.TokenResponseDTO;
import com.planup.planup.validation.jwt.service.TokenService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserAuthCommandServiceImpl implements UserAuthCommandService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;
    private final TermsRepository termsRepository;
    private final UserTermsRepository userTermsRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final FriendRepository friendRepository;
    private final UserWithdrawalRepository userWithdrawalRepository;
    private final KaKaoService kakaoService;
    private final UserAuthConverter userAuthConverter;
    private final UserQueryService userQueryService;

    @Qualifier("objectRedisTemplate")
    private final RedisTemplate<String, Object> objectRedisTemplate;

    @Value("${app.domain:http://localhost:8080}")
    private String appDomain;

    private static final String TEMP_USER_PREFIX = "temp_kakao_user:";
    private static final String TEMP_PROFILE_PREFIX = "temp_profile:";
    private static final int TEMP_USER_EXPIRE_MINUTES = 60;

    // ========== 회원가입/로그인 ==========

    @Override
    public UserResponseDTO.Signup signup(UserRequestDTO.Signup request) {
        userQueryService.checkEmail(request.getEmail());

        if (!request.getPassword().equals(request.getPasswordCheck())) {
            throw new UserException(ErrorStatus.PASSWORD_MISMATCH);
        }

        validateRequiredTerms(request.getAgreements());

        if (!userQueryService.isEmailVerified(request.getEmail())) {
            throw new UserException(ErrorStatus.EMAIL_VERIFICATION_REQUIRED);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Object tempImageUrlObj = objectRedisTemplate.opsForValue().get(TEMP_PROFILE_PREFIX + request.getEmail());
        String tempImageUrl = (tempImageUrlObj != null) ? (String) tempImageUrlObj : null;

        String requestProfileImg = request.getProfileImg();
        if (requestProfileImg != null && requestProfileImg.trim().isEmpty()) {
            requestProfileImg = null;
        }

        String profileImgUrl = (tempImageUrl != null) ? tempImageUrl : requestProfileImg;

        User user = userAuthConverter.toUserEntity(request, encodedPassword, profileImgUrl);

        User savedUser = userRepository.save(user);

        addTermsAgreements(savedUser, request.getAgreements());

        TokenResponseDTO tokenResponse = tokenService.generateTokens(savedUser);

        clearVerificationToken(request.getEmail());

        if (tempImageUrl != null) {
            objectRedisTemplate.delete(TEMP_PROFILE_PREFIX + request.getEmail());
        }

        return userAuthConverter.toSignupResponseDTO(savedUser,
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                tokenResponse.getExpiresIn()
        );
    }

    @Override
    public UserResponseDTO.Login login(UserRequestDTO.Login request) {
        User user = userRepository.findByEmailAndUserActivate(request.getEmail(), UserActivate.ACTIVE)
                .orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_USER));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserException(ErrorStatus.INVALID_CREDENTIALS);
        }

        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().toString(), user.getId());

        return userAuthConverter.toLoginResponseDTO(user, accessToken);
    }

    @Override
    public void logout(Long userId, jakarta.servlet.http.HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = jwtUtil.extractTokenFromHeader(authHeader);
            if (accessToken != null) {
                tokenService.blacklistAccessToken(accessToken);
            }
        }

        tokenService.logout(userId);

        log.info("로그아웃 완료 - 사용자 ID: {}", userId);
    }

    @Override
    public UserResponseDTO.Withdrawal withdrawUser(Long userId, UserRequestDTO.Withdrawal request) {
        User user = userQueryService.getUserByUserId(userId);

        UserWithdrawal withdrawal = userAuthConverter.toUserWithdrawalEntity(user, request.getReason());
        userWithdrawalRepository.save(withdrawal);

        user.setUserActivate(UserActivate.INACTIVE);
        userRepository.save(user);

        cleanupUserData(user);

        log.info("사용자 {} 회원 탈퇴 완료. 이유: {}", user.getNickname(), request.getReason());

        return userAuthConverter.toWithdrawalResponseDTO(true, "회원 탈퇴가 완료되었습니다.", LocalDateTime.now().toString());
    }

    private void cleanupUserData(User user) {
        try {
            List<Friend> userFriends = friendRepository.findByStatusAndUserIdOrStatusAndFriendIdOrderByCreatedAtDesc(
                    FriendStatus.ACCEPTED, user.getId(), FriendStatus.ACCEPTED, user.getId());
            friendRepository.deleteAll(userFriends);

            List<Friend> friendRequests = friendRepository.findByStatusAndFriend_IdOrderByCreatedAtDesc(
                    FriendStatus.REQUESTED, user.getId());
            friendRepository.deleteAll(friendRequests);

        } catch (Exception e) {
        }
    }

    // ========== 카카오 OAuth ==========

    @Override
    public OAuthResponseDTO.KakaoAuth kakaoAuth(OAuthRequestDTO.KakaoAuth request) {
        KakaoUserInfo kakaoUserInfo = kakaoService.getUserInfo(request.getCode());
        String email = kakaoUserInfo.getKakaoAccount().getEmail();
        return handleKakaoAuth(kakaoUserInfo, email);
    }

    @Override
    public UserResponseDTO.Signup kakaoSignupComplete(OAuthRequestDTO.KaKaoSignup request) {
        String redisKey = TEMP_USER_PREFIX + request.getTempUserId();
        KakaoUserInfo kakaoUserInfo = (KakaoUserInfo) objectRedisTemplate.opsForValue().get(redisKey);

        if (kakaoUserInfo == null) {
            throw new UserException(ErrorStatus.NOT_FOUND_USER);
        }

        User user = createBasicKakaoUser(kakaoUserInfo, request);

        Object tempImageUrlObj = objectRedisTemplate.opsForValue().get(TEMP_PROFILE_PREFIX + kakaoUserInfo.getKakaoAccount().getEmail());
        String tempImageUrl = (tempImageUrlObj != null) ? (String) tempImageUrlObj : null;

        String requestProfileImg = request.getProfileImg();
        if (requestProfileImg != null && requestProfileImg.trim().isEmpty()) {
            requestProfileImg = null;
        }

        String profileImgUrl = (tempImageUrl != null) ? tempImageUrl : requestProfileImg;
        if (profileImgUrl != null) {
            user.updateProfileImage(profileImgUrl);
            userRepository.save(user);
        }

        if (request.getNickname() != null) {
            processNickname(user, request.getNickname());
        }

        if (request.getAgreements() != null) {
            addTermsAgreements(user, request.getAgreements());
        }

        objectRedisTemplate.delete(redisKey);

        if (tempImageUrl != null) {
            objectRedisTemplate.delete(TEMP_PROFILE_PREFIX + kakaoUserInfo.getKakaoAccount().getEmail());
        }

        TokenResponseDTO tokenResponse = tokenService.generateTokens(user);

        return userAuthConverter.toSignupResponseDTO(user,
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                tokenResponse.getExpiresIn()
        );
    }

    @Override
    public OAuthResponseDTO.KaKaoLink linkKakaoAccount(Long userId, OAuthRequestDTO.KaKaoLink request) {
        KakaoUserInfo kakaoUserInfo = kakaoService.getUserInfo(request.getCode());
        String email = kakaoUserInfo.getKakaoAccount().getEmail();

        User user = userQueryService.getUserByUserId(userId);

        Optional<OAuthAccount> existingOAuth = oAuthAccountRepository
                .findByUserAndProvider(user, AuthProvideerEnum.KAKAO);

        if (existingOAuth.isPresent()) {
            throw new UserException(ErrorStatus.KAKAO_ACCOUNT_ALREADY_LINKED);
        }

        Optional<OAuthAccount> otherUserOAuth = oAuthAccountRepository
                .findByEmailAndProvider(email, AuthProvideerEnum.KAKAO);

        if (otherUserOAuth.isPresent()) {
            throw new UserException(ErrorStatus.KAKAO_ACCOUNT_ALREADY_USED);
        }

        OAuthAccount oAuthAccount = userAuthConverter.toOAuthAccountEntity(user, email, AuthProvideerEnum.KAKAO);
        oAuthAccountRepository.save(oAuthAccount);

        return userAuthConverter.toKakaoLinkResponseDTO(true, "카카오 계정 연동이 완료되었습니다", email, userAuthConverter.toUserInfoResponseDTO(user));
    }

    private OAuthResponseDTO.KakaoAuth handleKakaoAuth(KakaoUserInfo kakaoUserInfo, String email) {
        Optional<OAuthAccount> existingOAuth = oAuthAccountRepository
                .findByEmailAndProvider(email, AuthProvideerEnum.KAKAO);

        if (existingOAuth.isPresent()) {
            User user = existingOAuth.get().getUser();

            if (user.getUserActivate() != UserActivate.ACTIVE) {
                throw new UserException(ErrorStatus.USER_INACTIVE);
            }

            TokenResponseDTO tokenResponse = tokenService.generateTokens(user);

            return userAuthConverter.toKakaoAuthResponseDTO(
                    false,
                    tokenResponse.getAccessToken(),
                    tokenResponse.getRefreshToken(),
                    tokenResponse.getExpiresIn(),
                    UserResponseDTO.UserInfo.from(user)
            );

        } else {
            String tempUserId = UUID.randomUUID().toString();
            String redisKey = TEMP_USER_PREFIX + tempUserId;

            objectRedisTemplate.opsForValue().set(redisKey, kakaoUserInfo, Duration.ofMinutes(TEMP_USER_EXPIRE_MINUTES));

            return userAuthConverter.toKakaoAuthResponseDTO(true, tempUserId);
        }
    }

    private User createBasicKakaoUser(KakaoUserInfo kakaoUserInfo, OAuthRequestDTO.KaKaoSignup request) {
        if (request.getAgreements() != null) {
            validateRequiredTerms(request.getAgreements());
        }

        User user =  userAuthConverter.toKakaoUserEntity(kakaoUserInfo, request);
        User savedUser = userRepository.save(user);

        OAuthAccount oAuthAccount = userAuthConverter.toOAuthAccountEntity(savedUser, kakaoUserInfo.getKakaoAccount().getEmail(), AuthProvideerEnum.KAKAO);
        oAuthAccountRepository.save(oAuthAccount);

        return savedUser;
    }

    private void processNickname(User user, String nickname) {
        if (user.getNickname().equals(nickname)) {
            return;
        }

        if (userRepository.existsByNickname(nickname)) {
            throw new UserException(ErrorStatus.EXIST_NICKNAME);
        }

        user.setNickname(nickname);
        userRepository.save(user);
    }

    // ========== 초대 코드 ==========

    @Override
    public AuthResponseDTO.InviteCodeProcess processInviteCode(String inviteCode, Long userId) {
        if (inviteCode == null || inviteCode.trim().isEmpty()) {
            throw new UserException(ErrorStatus.INVALID_INVITE_CODE);
        }

        Long inviterId = findInviterByCode(inviteCode);

        if (inviterId == null || inviterId.equals(userId)) {
            throw new UserException(ErrorStatus.INVALID_INVITE_CODE);
        }

        User currentUser = userQueryService.getUserByUserId(userId);
        User inviterUser = userQueryService.getUserByUserId(inviterId);

        boolean alreadyFriend = friendRepository.findByUserAndFriend_NicknameAndStatus(
                currentUser, inviterUser.getNickname(), FriendStatus.ACCEPTED).isPresent() ||
                friendRepository.findByUserAndFriend_NicknameAndStatus(
                        inviterUser, currentUser.getNickname(), FriendStatus.ACCEPTED).isPresent();

        if (alreadyFriend) {
            throw new UserException(ErrorStatus.INVALID_INVITE_CODE);
        }

        Friend friendship1 = userAuthConverter.toFriendEntity(currentUser, inviterUser, FriendStatus.ACCEPTED);
        Friend friendship2 = userAuthConverter.toFriendEntity(inviterUser, currentUser, FriendStatus.ACCEPTED);

        friendRepository.save(friendship1);
        friendRepository.save(friendship2);

        return userAuthConverter.toInviteCodeProcessResponseDTO(true, inviterUser.getNickname(), "친구 관계가 성공적으로 생성되었습니다.");
    }

    private Long findInviterByCode(String inviteCode) {
        String inviterIdStr = redisTemplate.opsForValue().get("invite_code:" + inviteCode);
        return (inviterIdStr != null) ? Long.parseLong(inviterIdStr) : null;
    }

    // ========== 회원가입 이메일 인증 ==========

    @Override
    public AuthResponseDTO.EmailSend sendEmailVerification(String email) {
        userQueryService.checkEmail(email);

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

        return userAuthConverter.toEmailSendResponseDTO(email, verificationToken, "인증 메일이 발송되었습니다");
    }

    @Override
    public AuthResponseDTO.EmailSend resendEmailVerification(String email) {
        if (userQueryService.isEmailVerified(email)) {
            throw new UserException(ErrorStatus.EMAIL_ALREADY_VERIFIED);
        }

        clearExistingTokens(email);
        return sendEmailVerification(email);
    }

    @Override
    public String handleEmailVerificationLink(String token) {
        String email = completeVerification(token);

        String deepLinkUrl = "planup://profile/setup?email=" +
                java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8) +
                "&verified=true&token=" + token +
                "&from=email_verification";

        return EmailTemplateUtil.createSuccessHtml(email, deepLinkUrl);
    }

    private String completeVerification(String verificationToken) {
        String email;
        try {
            email = getEmailByToken(verificationToken);
        } catch (IllegalArgumentException e) {
            log.error("토큰 검증 실패: {}", e.getMessage());
            throw new IllegalArgumentException("토큰이 만료되었거나 이미 사용되었습니다.");
        }

        if (userQueryService.isEmailVerified(email)) {
            return email;
        }

        try {
            redisTemplate.opsForValue().set(
                    "email-verified:" + email,
                    "VERIFIED",
                    60,
                    TimeUnit.MINUTES
            );
        } catch (Exception e) {
            log.error("Redis 저장 실패: {}", e.getMessage());
            throw new RuntimeException("이메일 인증 처리 중 오류가 발생했습니다.");
        }

        return email;
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

    private void clearVerificationToken(String email) {
        redisTemplate.delete("email-verified:" + email);
        clearExistingTokens(email);
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

    // ========== 비밀번호 변경 ==========

    @Override
    public void changePasswordWithToken(String token, String newPassword) {
        String[] tokenInfo = validatePasswordChangeToken(token);
        String email = tokenInfo[0];

        User user = userRepository.findByEmailAndUserActivate(email, UserActivate.ACTIVE)
                .orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_USER));

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);

        userRepository.save(user);

        clearPasswordChangeToken(email);
    }

    @Override
    public AuthResponseDTO.EmailSend sendPasswordChangeEmail(String email, Boolean isLoggedIn) {
        userQueryService.checkEmailExists(email);

        String token = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(
                "password-change:" + token,
                email + ":" + isLoggedIn,
                30,
                TimeUnit.MINUTES
        );

        String changeUrl = appDomain + "/users/password/change-link?token=" + token;
        sendPasswordChangeEmailContent(email, changeUrl);

        return userAuthConverter.toEmailSendResponseDTO(email, token, "비밀번호 변경 메일이 발송되었습니다.");
    }

    @Override
    public AuthResponseDTO.EmailSend resendPasswordChangeEmail(String email, Boolean isLoggedIn) {
        userQueryService.checkEmailExists(email);

        clearExistingPasswordChangeTokens(email);
        return sendPasswordChangeEmail(email, isLoggedIn);
    }

    @Override
    public String handlePasswordChangeLink(String token) {
        String[] tokenInfo = validatePasswordChangeToken(token);
        String email = tokenInfo[0];
        Boolean isLoggedIn = Boolean.parseBoolean(tokenInfo[1]);

        markPasswordChangeEmailAsVerified(email);

        String deepLinkUrl;
        if (isLoggedIn) {
            deepLinkUrl = "planup://mypage/password/change?email=" +
                    java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8) +
                    "&verified=true&token=" + token +
                    "&from=password_change&loggedIn=true";
        } else {
            deepLinkUrl = "planup://login/password/change?email=" +
                    java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8) +
                    "&verified=true&token=" + token +
                    "&from=password_change&loggedIn=false";
        }

        return EmailTemplateUtil.createSuccessHtml(email, deepLinkUrl);
    }

    @Override
    public void markPasswordChangeEmailAsVerified(String email) {
        redisTemplate.opsForValue().set(
                "password-change-verified:" + email,
                "VERIFIED",
                24,
                TimeUnit.HOURS
        );
    }

    private String[] validatePasswordChangeToken(String token) {
        String value = redisTemplate.opsForValue().get("password-change:" + token);

        if (value == null) {
            throw new IllegalArgumentException("만료되거나 유효하지 않은 비밀번호 변경 토큰입니다.");
        }

        String[] parts = value.split(":");
        return new String[]{parts[0], parts[1]};
    }

    private void clearPasswordChangeToken(String email) {
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

    private void clearExistingPasswordChangeTokens(String email) {
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

    // ========== 약관 동의 ==========

    private void validateRequiredTerms(List<AuthRequestDTO.TermsAgreement> agreements) {
        List<Terms> requiredTerms = termsRepository.findByIsRequiredTrue();

        List<Long> agreedRequiredTermsIds = agreements.stream()
                .filter(AuthRequestDTO.TermsAgreement::isAgreed)
                .map(AuthRequestDTO.TermsAgreement::getTermsId)
                .toList();

        for (Terms requiredTerm : requiredTerms) {
            if (!agreedRequiredTermsIds.contains(requiredTerm.getId())) {
                throw new UserException(ErrorStatus.REQUIRED_TERMS_NOT_AGREED);
            }
        }
    }

    private void addTermsAgreements(User user, List<AuthRequestDTO.TermsAgreement> agreements) {
        for (AuthRequestDTO.TermsAgreement agreement : agreements) {
            Terms terms = termsRepository.findById(agreement.getTermsId())
                    .orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_TERMS));

            UserTerms userTerms = userAuthConverter.toUserTermsEntity(user, terms, agreement);
            userTermsRepository.save(userTerms);
        }
    }

    // ========== Private 헬퍼 메서드 ==========

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
}