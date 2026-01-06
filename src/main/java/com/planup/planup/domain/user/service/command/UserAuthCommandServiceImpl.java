package com.planup.planup.domain.user.service.command;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.AuthException;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.bedge.entity.UserStat;
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
import com.planup.planup.domain.user.enums.UserActivate;
import com.planup.planup.domain.user.repository.*;
import com.planup.planup.domain.user.service.external.KaKaoService;
import com.planup.planup.domain.user.service.query.UserQueryService;
import com.planup.planup.domain.user.service.util.EmailTemplateUtil;
import com.planup.planup.validation.jwt.JwtUtil;
import com.planup.planup.validation.jwt.dto.TokenResponseDTO;
import com.planup.planup.validation.jwt.service.TokenService;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
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

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private final UserStatRepository userStatRepository;

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

        // 사용자 생성
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        String profileImgUrl = determineProfileImageUrl(request.getEmail(), request.getProfileImg());
        User user = userAuthConverter.toUserEntity(request, encodedPassword, profileImgUrl);

        //유저 스텟 클래스 추가
        UserStat userStat = new UserStat();
        user.setUserStat(userStat);
        userStatRepository.save(userStat);

        User savedUser = userRepository.save(user);

        // 약관 저장
        addTermsAgreements(savedUser, request.getAgreements());

        // 토큰 발급
        TokenResponseDTO tokenResponse = tokenService.generateTokens(savedUser);

        // 정리 작업
        cleanupAfterSignup(request.getEmail());

        log.info("회원가입 완료: userId={}, email={}", savedUser.getId(), savedUser.getEmail());

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
    public void logout(Long userId, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String accessToken = jwtUtil.extractTokenFromHeader(authHeader);
        tokenService.blacklistAccessToken(accessToken);
        tokenService.logout(userId); // 리프레시 토큰 삭제
    }

    @Override
    public UserResponseDTO.Withdrawal withdrawUser(Long userId, UserRequestDTO.Withdrawal request) {
        User user = userQueryService.getUserByUserId(userId);

        // 탈퇴 기록 저장
        UserWithdrawal withdrawal = userAuthConverter.toUserWithdrawalEntity(user, request.getReason());
        userWithdrawalRepository.save(withdrawal);

        // 연관 데이터 정리
        cleanupUserData(user);

        userRepository.delete(user);

        log.info("사용자 {} 회원 탈퇴 완료. 이유: {}", user.getNickname(), request.getReason());
        return userAuthConverter.toWithdrawalResponseDTO(true, "회원 탈퇴가 완료되었습니다.", LocalDateTime.now().toString());
    }

    private void cleanupUserData(User user) {
        try {
            // 친구 관계 삭제
            List<Friend> userFriends = friendRepository.findByStatusAndUserIdOrStatusAndFriendIdOrderByCreatedAtDesc(
                    FriendStatus.ACCEPTED, user.getId(), FriendStatus.ACCEPTED, user.getId());
            friendRepository.deleteAll(userFriends);

            // 친구 요청 삭제
            List<Friend> friendRequests = friendRepository.findByStatusAndFriend_IdOrderByCreatedAtDesc(
                    FriendStatus.REQUESTED, user.getId());
            friendRepository.deleteAll(friendRequests);

        } catch (Exception e) {
            throw new UserException(ErrorStatus.NOT_FOUND_USER);
        }
    }

    // ========== 카카오 OAuth ==========

    @Override
    public OAuthResponseDTO.KakaoAuth kakaoAuth(OAuthRequestDTO.KakaoAuth request) {
        KakaoUserInfo kakaoUserInfo = kakaoService.getUserInfo(request.getCode());
        if (kakaoUserInfo == null) {
            throw new AuthException(ErrorStatus.KAKAO_USER_INFO_FAILED);
        }
        String email = Optional.ofNullable(kakaoUserInfo.getKakaoAccount())
                .map(account -> account.getEmail())
                .orElseThrow(() -> new AuthException(ErrorStatus.KAKAO_USER_INFO_FAILED));

        return handleKakaoAuth(kakaoUserInfo, email);
    }

    @Override
    public UserResponseDTO.Signup kakaoSignupComplete(OAuthRequestDTO.KaKaoSignup request) {
        // Redis에서 임시 유저 정보 조회
        String redisKey = TEMP_USER_PREFIX + request.getTempUserId();
        KakaoUserInfo kakaoUserInfo;
        try {
            kakaoUserInfo = (KakaoUserInfo) objectRedisTemplate.opsForValue().get(redisKey);
        } catch (Exception e) {
            throw new UserException(ErrorStatus.SIGNUP_TIME_OUT); // Redis 데이터가 깨져있거나 연결 문제 시
        }

        if (kakaoUserInfo == null) {
            throw new UserException(ErrorStatus.SIGNUP_TIME_OUT);
        }

        User user = createBasicKakaoUser(kakaoUserInfo, request);

        processProfileImage(user, kakaoUserInfo.getKakaoAccount().getEmail(), request.getProfileImg());

        if (request.getNickname() != null) {
            processNickname(user, request.getNickname());
        }

        if (request.getAgreements() != null) {
            addTermsAgreements(user, request.getAgreements()); // 약관 동의 저장
        }

        // Redis 데이터 정리
        cleanupRedisData(redisKey, kakaoUserInfo.getKakaoAccount().getEmail());

        // 토큰 발급 및 응답
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
        if (kakaoUserInfo == null) {
            throw new AuthException(ErrorStatus.KAKAO_USER_INFO_FAILED);
        }

        String email = Optional.ofNullable(kakaoUserInfo.getKakaoAccount())
                .map(account -> account.getEmail())
                .orElseThrow(() -> new AuthException(ErrorStatus.KAKAO_EMAIL_NOT_FOUND));

        User user = userQueryService.getUserByUserId(userId);

        // 이미 연동되어 있는지 확인
        boolean isAlreadyLinked = oAuthAccountRepository.existsByUserAndProvider(user, AuthProvideerEnum.KAKAO);

        if (isAlreadyLinked) {
            throw new UserException(ErrorStatus.KAKAO_ACCOUNT_ALREADY_LINKED);
        }

        // 이미 다른 유저와 연동되었는지 확인
        Optional<OAuthAccount> otherUserOAuth = oAuthAccountRepository
                .findByEmailAndProvider(email, AuthProvideerEnum.KAKAO);

        if (otherUserOAuth.isPresent()) {
            throw new UserException(ErrorStatus.KAKAO_ACCOUNT_ALREADY_USED);
        }

        // 연동 정보 저장
        OAuthAccount oAuthAccount = userAuthConverter.toOAuthAccountEntity(user, email, AuthProvideerEnum.KAKAO);
        oAuthAccountRepository.save(oAuthAccount);

        log.info("카카오 계정 연동 성공 (User ID: {}, Email: {})", userId, email);

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

            try {
                // Redis 저장 시도
                objectRedisTemplate.opsForValue().set(
                        redisKey,
                        kakaoUserInfo,
                        Duration.ofMinutes(TEMP_USER_EXPIRE_MINUTES)
                );

            } catch (Exception e) {
                // Redis 연결 실패 등을 잡아서 처리
                throw new AuthException(ErrorStatus.REDIS_SAVE_FAILED);
            }
            return userAuthConverter.toKakaoAuthResponseDTO(true, tempUserId);
        }
    }

    private User createBasicKakaoUser(KakaoUserInfo kakaoUserInfo, OAuthRequestDTO.KaKaoSignup request) {
        if (request.getAgreements() != null) {
            validateRequiredTerms(request.getAgreements());
        }
        User user =  userAuthConverter.toKakaoUserEntity(kakaoUserInfo, request);

        UserStat userStat = new UserStat();
        user.setUserStat(userStat);

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

        // 유효하지 않은 코드 or 자기 자신 초대 방지
        if (inviterId.equals(userId)) {
            throw new UserException(ErrorStatus.INVALID_INVITE_CODE);
        }

        User currentUser = userQueryService.getUserByUserId(userId);
        User inviterUser = userQueryService.getUserByUserId(inviterId);

        // 이미 친구인지 확인
        boolean alreadyFriend = friendRepository.existsByUserAndFriendAndStatus(currentUser, inviterUser, FriendStatus.ACCEPTED);

        if (alreadyFriend) {
            throw new UserException(ErrorStatus.ALREADY_FRIEND);
        }

        // 친구 관계 양방향 저장
        Friend friendship1 = userAuthConverter.toFriendEntity(currentUser, inviterUser, FriendStatus.ACCEPTED);
        Friend friendship2 = userAuthConverter.toFriendEntity(inviterUser, currentUser, FriendStatus.ACCEPTED);

        friendRepository.save(friendship1);
        friendRepository.save(friendship2);

        return userAuthConverter.toInviteCodeProcessResponseDTO(true, inviterUser.getNickname(), "친구 관계가 성공적으로 생성되었습니다.");
    }

    private Long findInviterByCode(String inviteCode) {
        String inviterIdStr = redisTemplate.opsForValue().get("invite_code:" + inviteCode);
        // Redis에 키가 없거나 만료된 경우
        if (inviterIdStr == null) {
            throw new UserException(ErrorStatus.INVALID_INVITE_CODE);
        }

        try {
            // 숫자로 변환하여 반환
            return Long.parseLong(inviterIdStr);
        } catch (NumberFormatException e) {
            // 만약 Redis에 저장된 값이 숫자가 아닌 이상한 값일 경우
            throw new UserException(ErrorStatus.INVALID_INVITE_CODE);
        }
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
            throw new AuthException(ErrorStatus.EMAIL_TOKEN_SAVE_FAILED);
        }

        String verificationUrl = appDomain + "/users/email/verify-link?token=" + verificationToken;

        // 이메일 발송
        try {
            sendEmail(email, verificationUrl);
        } catch (Exception e) {
            log.error("이메일 발송 실패 (Email: {}): {}", email, e.getMessage());
            redisTemplate.delete("email-verification:" + verificationToken); // 발송 실패 시 Redis에 저장된 토큰 삭제
            throw new AuthException(ErrorStatus.EMAIL_SEND_FAILED);
        }

        return userAuthConverter.toEmailSendResponseDTO(email, verificationToken, "인증 메일이 발송되었습니다");
    }

    @Override
    public AuthResponseDTO.EmailSend resendEmailVerification(String email) {
        if (userQueryService.isEmailVerified(email)) {
            throw new UserException(ErrorStatus.EMAIL_ALREADY_VERIFIED);
        }

        return sendEmailVerification(email);
    }

    @Override
    public String handleEmailVerificationLink(String token) {
        String email = completeVerification(token);

        try {
            // 딥링크 생성
            String deepLinkUrl = "planup://profile/setup?email=" +
                    java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8.toString()) +
                    "&verified=true&token=" + token +
                    "&from=email_verification";

            return EmailTemplateUtil.createSuccessHtml(email, deepLinkUrl);

        } catch (UnsupportedEncodingException e) {
            throw new AuthException(ErrorStatus.URL_ENCODING_FAILED);
        }
    }

    private String completeVerification(String verificationToken) {
        String email = getEmailByToken(verificationToken);

        if (userQueryService.isEmailVerified(email)) {
            return email;
        }

        try {
            // 인증 완료 상태를 Redis에 저장 (회원가입 진행용, 60분 유효)
            redisTemplate.opsForValue().set(
                    "email-verified:" + email,
                    "VERIFIED",
                    60,
                    TimeUnit.MINUTES
            );

            // 사용한 인증 토큰은 삭제 (재사용 방지)
            redisTemplate.delete("email-verification:" + verificationToken);

            return email;

        } catch (Exception e) {
            log.error("이메일 인증 상태 Redis 저장 실패: {}", e.getMessage());
            throw new AuthException(ErrorStatus.EMAIL_VERIFICATION_FAILED);
        }
    }

    private String getEmailByToken(String token) {
        // Redis 조회 시 연결 에러 등이 나면 RuntimeException이 터져서 전역 핸들러로 감
        String email = redisTemplate.opsForValue().get("email-verification:" + token);

        if (email == null) {
            throw new AuthException(ErrorStatus.INVALID_EMAIL_TOKEN);
        }
        return email;
    }

    private void clearVerificationToken(String email) {
        redisTemplate.delete("email-verified:" + email);
    }

    // ========== 비밀번호 변경 ==========

    @Override
    public void changePassword(Long userId,String newPassword) {

        User user = userRepository.findByIdAndUserActivate(userId, UserActivate.ACTIVE)
                .orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_USER));

        // 비밀번호 변경
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    @Override
    public AuthResponseDTO.EmailSend sendPasswordChangeEmail(String email, Boolean isLoggedIn) {
        userQueryService.checkEmailExists(email);

        String token = UUID.randomUUID().toString();
        String value = email + ":" + isLoggedIn; // "이메일:로그인여부"

        try {
            redisTemplate.opsForValue().set(
                    "password-change:" + token,
                    value,
                    30,
                    TimeUnit.MINUTES
            );
        } catch (Exception e) {
            log.error("비밀번호 변경 토큰 Redis 저장 실패: {}", e.getMessage());
            throw new AuthException(ErrorStatus.PASSWORD_CHANGE_FAILED);
        }

        String changeUrl = appDomain + "/users/password/change-link?token=" + token;

        try {
            // 이메일 발송
            sendPasswordChangeEmailContent(email, changeUrl);
        } catch (Exception e) {
            log.error("비밀번호 변경 메일 발송 실패: {}", e.getMessage());
            redisTemplate.delete("password-change:" + token); // 실패 시 Redis 토큰도 정리
            throw new AuthException(ErrorStatus.EMAIL_SEND_FAILED);
        }

        return userAuthConverter.toEmailSendResponseDTO(email, token, "비밀번호 변경 메일이 발송되었습니다.");
    }

    @Override
    public AuthResponseDTO.EmailSend resendPasswordChangeEmail(String email, Boolean isLoggedIn) {
        userQueryService.checkEmailExists(email);
        return sendPasswordChangeEmail(email, isLoggedIn);
    }

    @Override
    public String handlePasswordChangeLink(String token) {
        String[] tokenInfo = validatePasswordChangeToken(token);
        String email = tokenInfo[0];
        boolean isLoggedIn = Boolean.parseBoolean(tokenInfo[1]);

        // 인증됨 상태 마킹
        markPasswordChangeEmailAsVerified(email);

        try {
            String encodedEmail = java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8.toString());

            String deepLinkUrl = String.format("planup://%s/password/change?email=%s&verified=true&token=%s&from=password_change&loggedIn=%s",
                    isLoggedIn ? "mypage" : "login",
                    encodedEmail,
                    token,
                    isLoggedIn
            );

            return EmailTemplateUtil.createSuccessHtml(email, deepLinkUrl);

        } catch (UnsupportedEncodingException e) {
            log.error("비밀번호 변경 딥링크 인코딩 실패: {}", e.getMessage());
            throw new AuthException(ErrorStatus.URL_ENCODING_FAILED);
        }
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
            throw new AuthException(ErrorStatus.PASSWORD_CHANGE_TOKEN_INVALID);
        }

        // 데이터 포맷 검증
        String[] parts = value.split(":");
        if (parts.length < 2) {
            throw new AuthException(ErrorStatus.PASSWORD_CHANGE_TOKEN_INVALID);
        }

        return parts;
    }

    private void cleanupUsedTokens(String token, String email) {
        try {
            // 사용한 토큰 삭제
            redisTemplate.delete("password-change:" + token);
            // 인증 마킹 삭제
            redisTemplate.delete("password-change-verified:" + email);
        } catch (Exception e) {
            log.warn("비밀번호 변경 후 Redis 정리 실패 (데이터 불일치 아님): {}", e.getMessage());
        }
    }

    // ========== 약관 동의 ==========

    private void validateRequiredTerms(List<AuthRequestDTO.TermsAgreement> agreements) {
        List<Terms> requiredTerms = termsRepository.findByIsRequiredTrue();

        Set<Long> agreedTermsIds = agreements.stream()
                .filter(AuthRequestDTO.TermsAgreement::isAgreed)
                .map(AuthRequestDTO.TermsAgreement::getTermsId)
                .collect(Collectors.toSet());

        for (Terms requiredTerm : requiredTerms) {
            if (!agreedTermsIds.contains(requiredTerm.getId())) {
                throw new UserException(ErrorStatus.REQUIRED_TERMS_NOT_AGREED);
            }
        }
    }

    private void addTermsAgreements(User user, List<AuthRequestDTO.TermsAgreement> agreements) {
        List<Long> termsIds = agreements.stream()
                .map(AuthRequestDTO.TermsAgreement::getTermsId)
                .toList();

        // 약관 엔티티 한 번에 조회
        List<Terms> foundTerms = termsRepository.findAllById(termsIds);

        if (foundTerms.size() != termsIds.size()) {
            throw new UserException(ErrorStatus.NOT_FOUND_TERMS);
        }

        // 매핑 편의를 위해 (ID -> Terms) 맵 생성
        Map<Long, Terms> termsMap = foundTerms.stream()
                .collect(Collectors.toMap(Terms::getId, terms -> terms));

        List<UserTerms> userTermsList = agreements.stream()
                .map(agreement -> {
                    Terms terms = termsMap.get(agreement.getTermsId());
                    return userAuthConverter.toUserTermsEntity(user, terms, agreement);
                })
                .toList();

        // 한 번에 저장 (Batch Insert) - N+1 문제 방지
        userTermsRepository.saveAll(userTermsList);
    }

    // ========== Private 헬퍼 메서드 ==========

    // 프로필 이미지 URL 결정
    private String determineProfileImageUrl(String email, String requestProfileImg) {
        // Redis에서 업로드된 임시 이미지 확인
        String uploadedImageUrl = getUploadedProfileImageSafely(email);
        if (uploadedImageUrl != null) {
            return uploadedImageUrl;
        }

        // 요청에 포함된 이미지 URL 확인
        if (requestProfileImg != null && !requestProfileImg.trim().isEmpty()) {
            return requestProfileImg.trim();
        }

        // 둘 다 없으면 null
        return null;
    }

    // Redis에서 업로드된 프로필 이미지 조회
    private String getUploadedProfileImageSafely(String email) {
        try {
            Object obj = objectRedisTemplate.opsForValue().get(TEMP_PROFILE_PREFIX + email);
            return (obj != null) ? (String) obj : null;
        } catch (Exception e) {
            log.warn("Redis 임시 프로필 이미지 조회 실패(회원가입은 계속 진행): email={}", email, e);
            return null;
        }
    }

    // [일반 회원가입용] 정리 작업
    private void cleanupAfterSignup(String email) {
        // 이메일 인증 토큰 삭제
        try {
            clearVerificationToken(email);
        } catch (Exception e) {
            log.warn("인증 토큰 삭제 실패: email={}", email, e);
        }
        // Redis 임시 프로필 이미지 삭제
        try {
            objectRedisTemplate.delete(TEMP_PROFILE_PREFIX + email);
        } catch (Exception e) {
            log.warn("Redis 임시 프로필 이미지 삭제 실패 (무시): email={}", email, e);
        }
    }

    private void processProfileImage(User user, String email, String requestProfileImg) {
        String finalProfileUrl = determineProfileImageUrl(email, requestProfileImg);

        if (finalProfileUrl != null) {
            user.updateProfileImage(finalProfileUrl);
            userRepository.save(user);
        }
    }

    // [카카오 회원가입용] 정리 작업
    private void cleanupRedisData(String userInfoKey, String email) {
        try {
            objectRedisTemplate.delete(userInfoKey); // 카카오 유저 정보 삭제
            objectRedisTemplate.delete(TEMP_PROFILE_PREFIX + email); // 임시 프로필 이미지 삭제
        } catch (Exception e) {
            log.warn("회원가입 후 Redis 데이터 정리 실패: {}", e.getMessage());
        }
    }

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
            log.error("이메일 발송 실패: {}", e.getMessage(), e);
            throw new AuthException(ErrorStatus.EMAIL_SEND_FAILED);
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
            log.error("비밀번호 변경 이메일 발송 실패: {}", e.getMessage(), e);
            throw new AuthException(ErrorStatus.EMAIL_SEND_FAILED);
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