package com.planup.planup.domain.user.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.friend.entity.FriendStatus;
import com.planup.planup.domain.friend.repository.FriendRepository;
import com.planup.planup.domain.global.service.ImageUploadService;
import com.planup.planup.domain.oauth.entity.OAuthAccount;
import com.planup.planup.domain.user.converter.UserConverter;
import com.planup.planup.domain.user.dto.*;
import com.planup.planup.domain.user.entity.*;
import com.planup.planup.domain.user.repository.InvitedUserRepository;
import com.planup.planup.domain.user.repository.TermsRepository;
import com.planup.planup.domain.user.repository.UserRepository;
import com.planup.planup.validation.jwt.JwtUtil;
import com.planup.planup.domain.user.repository.UserTermsRepository;
import com.planup.planup.domain.user.dto.UserInfoResponseDTO;
import com.planup.planup.domain.oauth.entity.AuthProvideerEnum;
import com.planup.planup.domain.oauth.repository.OAuthAccountRepository;
import com.planup.planup.domain.user.repository.UserWithdrawalRepository;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import com.planup.planup.domain.user.dto.KakaoAccountResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Builder
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TermsRepository termsRepository;
    private final UserTermsRepository userTermsRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final ImageUploadService imageUploadService;
    private final InviteCodeService inviteCodeService;
    private final InvitedUserRepository invitedUserRepository;
    private final FriendRepository friendRepository;
    private final EmailService emailService;
    private final UserWithdrawalRepository userWithdrawalRepository;
    private final KakaoApiService kakaoApiService;
    private final UserConverter userConverter;

    @Qualifier("objectRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

    // Redis 키 prefix
    private static final String TEMP_USER_PREFIX = "temp_kakao_user:";
    private static final String TEMP_PROFILE_PREFIX = "temp_profile:";
    private static final int TEMP_USER_EXPIRE_MINUTES = 60; // 60분 후 만료

    // 사용자 ID로 사용자 조회
    @Override
    public User getUserbyUserId(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_USER));
    }

    // 사용자 닉네임 조회
    @Override
    public String getNickname(Long userId) {
        User user = getUserbyUserId(userId);
        return user.getNickname();
    }

    // 사용자 닉네임 변경
    @Override
    public String updateNickname(Long userId, String nickname) {
        User user = getUserbyUserId(userId);

        // 현재 사용자가 이미 같은 닉네임을 사용하고 있는지 확인
        if (user.getNickname().equals(nickname)) {
            return nickname; // 같은 닉네임이면 그대로 반환
        }

        // 다른 사용자가 이미 사용 중인 닉네임인지 확인
        if (userRepository.existsByNickname(nickname)) {
            throw new UserException(ErrorStatus.EXIST_NICKNAME);
        }
        user.setNickname(nickname);
        return user.getNickname();
    }

    // 혜택 및 마케팅 알림 동의 상태 변경
    @Override
    public boolean updateNotificationAgree(Long userId) {
        User user = getUserbyUserId(userId);
        user.switchAlarmAllow();
        return user.getAlarmAllow();
    }

    // 비밀번호 변경 이메일 인증 완료 여부 확인
    @Override
    public Boolean isPasswordChangeEmailVerified(String email) {
        return emailService.isPasswordChangeEmailVerified(email);
    }

    // 토큰 기반 비밀번호 변경
    @Override
    public void changePasswordWithToken(String token, String newPassword) {
        // 토큰으로 이메일 검증
        String[] tokenInfo = emailService.validatePasswordChangeToken(token);
        String email = tokenInfo[0];  // 이메일만 추출
        
        // 해당 이메일의 사용자 조회
        User user = userRepository.findByEmailAndUserActivate(email, UserActivate.ACTIVE)
                .orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_USER));
        
        // 새 비밀번호 암호화 및 설정
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        
        // 사용자 저장
        userRepository.save(user);
        
        // 비밀번호 변경 완료 후 인증 토큰 정리
        emailService.clearPasswordChangeToken(email);
    }

    // 사용자 정보 조회
    @Override
    public UserInfoResponseDTO getUserInfo(Long userId) {
        User user = getUserbyUserId(userId);
        return userConverter.toUserInfoResponseDTO(user);
    }

    // 회원가입
    @Override
    public SignupResponseDTO signup(SignupRequestDTO request) {
        // 회원가입 요청 검증
        validateSignupRequest(request);

        // User 엔티티 생성 및 저장
        User savedUser = createUserFromSignupRequest(request);

        // 약관 동의 추가
        addTermsAgreements(savedUser, request.getAgreements());

        // 회원가입 완료 처리 (토큰 생성, 정리)
        return completeSignup(savedUser, request.getEmail());
    }

    // 회원가입 요청 검증
    private void validateSignupRequest(SignupRequestDTO request) {
        // 이메일 중복 체크
        checkEmail(request.getEmail());

        // 비밀번호 확인 검증
        if (!request.getPassword().equals(request.getPasswordCheck())) {
            throw new UserException(ErrorStatus.PASSWORD_MISMATCH);
        }

        // 필수 약관 동의 검증
        validateRequiredTerms(request.getAgreements());

        // 이메일 인증 여부 확인
        if (!emailService.isEmailVerified(request.getEmail())) {
            throw new UserException(ErrorStatus.EMAIL_VERIFICATION_REQUIRED);
        }
    }

    // 회원가입 요청으로부터 User 엔티티 생성
    private User createUserFromSignupRequest(SignupRequestDTO request) {
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        // 프로필 이미지 URL 결정 (Redis 임시 이미지 우선)
        String profileImgUrl = resolveProfileImageUrl(request.getEmail(), request.getProfileImg());

        // User 엔티티 생성 (converter 사용)
        User user = userConverter.toUserEntity(request, encodedPassword, profileImgUrl);

        return userRepository.save(user);
    }

    // 회원가입 완료 처리
    private SignupResponseDTO completeSignup(User user, String email) {
        // JWT 토큰 생성
        String accessToken = generateAccessToken(user);

        // 인증 토큰 정리
        emailService.clearVerificationToken(email);
        
        // 임시 프로필 이미지 URL 삭제
        clearTempProfileImage(email);

        // 응답 DTO 생성 (converter 사용)
        return userConverter.toSignupResponseDTO(user, accessToken);
    }

    // 이메일 변경
    @Override
    public String updateEmail(Long userId, String newEmail) {
        User user = getUserbyUserId(userId);

        // 현재 사용자가 이미 같은 이메일을 사용하고 있는지 확인
        if (user.getEmail().equals(newEmail)) {
            return newEmail; // 같은 이메일이면 그대로 반환
        }

        // 다른 사용자가 이미 사용 중인 이메일인지 확인
        if (userRepository.existsByEmailAndUserActivate(newEmail, UserActivate.ACTIVE)) {
            throw new UserException(ErrorStatus.USER_EMAIL_ALREADY_EXISTS);
        }

        user.setEmail(newEmail);
        return user.getEmail();
    }

    // 로그인
    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmailAndUserActivate(request.getEmail(), UserActivate.ACTIVE)
                .orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_USER));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserException(ErrorStatus.INVALID_CREDENTIALS);
        }

        // JWT 토큰 생성
        String accessToken = generateAccessToken(user);

        // 응답 DTO 생성 (converter 사용)
        return userConverter.toLoginResponseDTO(user, accessToken);
    }

    // 필수 약관 동의 검증
    private void validateRequiredTerms(List<TermsAgreementRequestDTO> agreements) {

        // 필수 약관 목록 조회
        List<Terms> requiredTerms = termsRepository.findByIsRequiredTrue();

        // 동의한 필수 약관 ID 목록
        List<Long> agreedRequiredTermsIds = agreements.stream()
                .filter(TermsAgreementRequestDTO::isAgreed)
                .map(TermsAgreementRequestDTO::getTermsId)
                .toList();

        // 필수 약관 중 동의하지 않은 것이 있는지 확인
        for (Terms requiredTerm : requiredTerms) {
            if (!agreedRequiredTermsIds.contains(requiredTerm.getId())) {
                throw new UserException(ErrorStatus.REQUIRED_TERMS_NOT_AGREED);
            }
        }
    }

    // 약관 동의 추가
    private void addTermsAgreements(User user, List<TermsAgreementRequestDTO> agreements) {

        for (TermsAgreementRequestDTO agreement : agreements) {

            Terms terms = termsRepository.findById(agreement.getTermsId())
                    .orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_TERMS));

            // UserTerms 엔티티 생성 (converter 사용)
            UserTerms userTerms = userConverter.toUserTermsEntity(user, terms, agreement);

            userTermsRepository.save(userTerms);
        }
    }
      
    // 카카오 계정 연동 상태 조회
    @Override
    public KakaoAccountResponseDTO getKakaoAccountStatus(Long userId) {
        User user = getUserbyUserId(userId);
        
        // 카카오톡 계정 정보 조회 (한 번에 조회)
        var oauthAccount = oAuthAccountRepository.findByUserAndProvider(user, AuthProvideerEnum.KAKAO);
        
        boolean isLinked = oauthAccount.isPresent();
        String kakaoEmail = oauthAccount.map(account -> account.getEmail()).orElse(null);
        
        return userConverter.toKakaoAccountResponseDTO(isLinked, kakaoEmail);
    }

    // 프로필 이미지 업로드
    @Override
    public ImageUploadResponseDTO uploadProfileImage(MultipartFile file, String email) {
        // 이미지 업로드
        String imageUrl = imageUploadService.uploadImage(file, "profile");
        
        // Redis에 임시 저장 (1시간 TTL)
        String redisKey = TEMP_PROFILE_PREFIX + email;
        redisTemplate.opsForValue().set(redisKey, imageUrl, Duration.ofHours(1));
        
        return userConverter.toImageUploadResponseDTO(imageUrl);
    }

    // 내 초대코드 조회
    @Override
    public InviteCodeResponseDTO getMyInviteCode(Long userId) {
        return inviteCodeService.getMyInviteCode(userId);
    }

    // 초대코드 처리 및 친구 관계 생성
    @Override
    public InviteCodeProcessResponseDTO processInviteCode(String inviteCode, Long userId) {
        // 빈 코드 체크
        if (inviteCode == null || inviteCode.trim().isEmpty()) {
            throw new UserException(ErrorStatus.INVALID_INVITE_CODE);
        }

        // InviteCodeService를 통해 초대자 찾기
        Long inviterId = inviteCodeService.findInviterByCode(inviteCode);

        if (inviterId == null) {
            throw new UserException(ErrorStatus.INVALID_INVITE_CODE);
        }

        // 본인 코드인지 확인
        if (inviterId.equals(userId)) {
            throw new UserException(ErrorStatus.INVALID_INVITE_CODE);
        }

        // 이미 친구인지 확인
        User currentUser = getUserbyUserId(userId);
        User inviterUser = getUserbyUserId(inviterId);
        
        boolean alreadyFriend = friendRepository.findByUserAndFriend_NicknameAndStatus(
                currentUser, inviterUser.getNickname(), FriendStatus.ACCEPTED).isPresent() ||
                friendRepository.findByUserAndFriend_NicknameAndStatus(
                        inviterUser, currentUser.getNickname(), FriendStatus.ACCEPTED).isPresent();

        if (alreadyFriend) {
            throw new UserException(ErrorStatus.INVALID_INVITE_CODE);
        }

        // 친구 관계 생성 (양방향) - converter 사용
        Friend friendship1 = userConverter.toFriendEntity(currentUser, inviterUser, FriendStatus.ACCEPTED);
        Friend friendship2 = userConverter.toFriendEntity(inviterUser, currentUser, FriendStatus.ACCEPTED);

        friendRepository.save(friendship1);
        friendRepository.save(friendship2);

        return userConverter.toInviteCodeProcessResponseDTO(true, inviterUser.getNickname(), "친구 관계가 성공적으로 생성되었습니다.");
    }

    // 초대코드 유효성 검증
    @Override
    public ValidateInviteCodeResponseDTO validateInviteCode(String inviteCode) {
        // 빈 코드 체크
        if (inviteCode == null || inviteCode.trim().isEmpty()) {
            throw new UserException(ErrorStatus.INVALID_INVITE_CODE);
        }

        // InviteCodeService를 통해 초대자 찾기
        Long inviterId = inviteCodeService.findInviterByCode(inviteCode);

        if (inviterId == null) {
            throw new UserException(ErrorStatus.INVALID_INVITE_CODE);
        }

        // 기본적인 유효성 검증 (초대코드 존재 여부, 초대자 정보)
        User inviterUser = getUserbyUserId(inviterId);

        return userConverter.toValidateInviteCodeResponseDTO(true, "유효한 초대코드입니다.", inviterUser.getNickname());
    }

    // 회원 탈퇴
    @Override
    public WithdrawalResponseDTO withdrawUser(Long userId, WithdrawalRequestDTO request) {
        // 사용자 조회
        User user = getUserbyUserId(userId);

        // 탈퇴 정보 저장 (converter 사용)
        UserWithdrawal withdrawal = userConverter.toUserWithdrawalEntity(user, request.getReason());
        userWithdrawalRepository.save(withdrawal);

        // 사용자 상태를 비활성화로 변경
        user.setUserActivate(UserActivate.INACTIVE);
        userRepository.save(user);

        // 관련 데이터 정리 (선택사항)
        cleanupUserData(user);

        log.info("사용자 {} 회원 탈퇴 완료. 이유: {}", user.getNickname(), request.getReason());

        return userConverter.toWithdrawalResponseDTO(true, "회원 탈퇴가 완료되었습니다.", LocalDateTime.now().toString());
    }

    // 사용자 관련 데이터 정리
    private void cleanupUserData(User user) {
        try {
            // 친구 관계 삭제
            List<Friend> userFriends = friendRepository.findByStatusAndUserIdOrStatusAndFriendIdOrderByCreatedAtDesc(
                FriendStatus.ACCEPTED, user.getId(), FriendStatus.ACCEPTED, user.getId());
            friendRepository.deleteAll(userFriends);

            // 친구 신청 삭제
            List<Friend> friendRequests = friendRepository.findByStatusAndFriend_IdOrderByCreatedAtDesc(
                FriendStatus.REQUESTED, user.getId());
            friendRepository.deleteAll(friendRequests);

            log.debug("사용자 {} 관련 데이터 정리 완료", user.getNickname());
        } catch (Exception e) {
            log.warn("사용자 데이터 정리 중 오류 발생: {}", e.getMessage());
            // 데이터 정리 실패는 탈퇴를 막지 않음
        }
    }

    // 회원가입 시 이메일 중복 체크
    @Override
    public void checkEmail(String email) {
        if (userRepository.existsByEmailAndUserActivate(email, UserActivate.ACTIVE)) {
            throw new UserException(ErrorStatus.USER_EMAIL_ALREADY_EXISTS);
        }
    }

    // 비밀번호 변경 시 이메일 존재 여부 체크
    @Override
    public void checkEmailExists(String email) {
        Optional<User> user = userRepository.findByEmailAndUserActivate(email, UserActivate.ACTIVE);
        if (user.isEmpty()) {
            throw new UserException(ErrorStatus.NOT_FOUND_USER);
        }
    }
    
    // 이메일 사용 가능 여부 확인 - 이미 존재하는 활성 사용자가 있으면 false, 없으면 true
    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmailAndUserActivate(email, UserActivate.ACTIVE);
    }

    // 비밀번호 변경 이메일 발송
    @Override
    public EmailSendResponseDTO sendPasswordChangeEmail(String email, Boolean isLoggedIn) {
        // 이메일이 등록된 사용자인지 확인
        checkEmailExists(email);

        // 비밀번호 변경 이메일 발송 (로그인 상태 정보 포함)
        String changeToken = emailService.sendPasswordChangeEmail(email, isLoggedIn);

        // 응답 DTO 생성
        return createEmailSendResponse(email, changeToken, "비밀번호 변경 확인 메일이 발송되었습니다");
    }

    // 비밀번호 변경 이메일 재발송
    @Override
    public EmailSendResponseDTO resendPasswordChangeEmail(String email, Boolean isLoggedIn) {
        // 이메일이 등록된 사용자인지 확인
        checkEmailExists(email);
    
        // 비밀번호 변경 이메일 재발송 (로그인 상태 포함)
        String changeToken = emailService.resendPasswordChangeEmail(email, isLoggedIn);
    
        return createEmailSendResponse(email, changeToken, "비밀번호 변경 확인 메일이 재발송되었습니다");
    }

    // 카카오 소셜 인증
    @Override
    public KakaoAuthResponseDTO kakaoAuth(KakaoAuthRequestDTO request) {
        KakaoUserInfo kakaoUserInfo = kakaoApiService.getUserInfo(request.getCode());
        String email = kakaoUserInfo.getEmail();
        return handleKakaoAuth(kakaoUserInfo, email);
    }

    // 카카오 계정 연동
    @Override
    public KakaoLinkResponseDTO linkKakaoAccount(Long userId, KakaoLinkRequestDTO request) {
        KakaoUserInfo kakaoUserInfo = kakaoApiService.getUserInfo(request.getCode());
        String email = kakaoUserInfo.getEmail();
        
        User user = getUserbyUserId(userId);
        
        // 이미 카카오 계정이 연동되어 있는지 확인
        Optional<OAuthAccount> existingOAuth = oAuthAccountRepository
                .findByUserAndProvider(user, AuthProvideerEnum.KAKAO);
        
        if (existingOAuth.isPresent()) {
            throw new UserException(ErrorStatus.KAKAO_ACCOUNT_ALREADY_LINKED);
        }
        
        // 다른 사용자가 이미 해당 카카오 계정을 사용하고 있는지 확인
        Optional<OAuthAccount> otherUserOAuth = oAuthAccountRepository
                .findByEmailAndProvider(email, AuthProvideerEnum.KAKAO);
        
        if (otherUserOAuth.isPresent()) {
            throw new UserException(ErrorStatus.KAKAO_ACCOUNT_ALREADY_USED);
        }
        
        // OAuth 계정 생성 및 연결 (converter 사용)
        OAuthAccount oAuthAccount = userConverter.toOAuthAccountEntity(user, email, AuthProvideerEnum.KAKAO);
        oAuthAccountRepository.save(oAuthAccount);
        
        // 연동 성공 응답 (converter 사용)
        return userConverter.toKakaoLinkResponseDTO(true, "카카오 계정 연동이 완료되었습니다", email, userConverter.toUserInfoResponseDTO(user));
    }

    // 카카오 로그인/회원가입 처리
    private KakaoAuthResponseDTO handleKakaoAuth(KakaoUserInfo kakaoUserInfo, String email) {
        // 기존 사용자 확인
        Optional<OAuthAccount> existingOAuth = oAuthAccountRepository
                .findByEmailAndProvider(email, AuthProvideerEnum.KAKAO);

        if (existingOAuth.isPresent()) {
            // 기존 사용자 - 기존 login() 로직 재사용
            User user = existingOAuth.get().getUser();

            if (user.getUserActivate() != UserActivate.ACTIVE) {
                throw new UserException(ErrorStatus.USER_INACTIVE);
            }

            String accessToken = generateAccessToken(user);

            // 응답 DTO 생성 (converter 사용)
            return userConverter.toKakaoAuthResponseDTO(false, accessToken, userConverter.toUserInfoResponseDTO(user));
        } else {
            // 신규 사용자 - Redis에 카카오 정보만 저장
            String tempUserId = UUID.randomUUID().toString();
            String redisKey = TEMP_USER_PREFIX + tempUserId;

            redisTemplate.opsForValue().set(redisKey, kakaoUserInfo, Duration.ofMinutes(TEMP_USER_EXPIRE_MINUTES));

            // 응답 DTO 생성 (converter 사용)
            return userConverter.toKakaoAuthResponseDTO(true, tempUserId);
        }
    }

    // 카카오 회원가입 완료
    @Override
    public SignupResponseDTO kakaoSignupComplete(KakaoSignupCompleteRequestDTO request) {
        // Redis에서 카카오 정보 조회 및 검증
        KakaoUserInfo kakaoUserInfo = getKakaoUserInfoFromRedis(request.getTempUserId());

        // 기본 사용자 생성
        User user = createBasicKakaoUser(kakaoUserInfo, request);

        // 프로필 이미지 및 닉네임 처리
        processKakaoUserProfile(user, kakaoUserInfo.getEmail(), request);

        // 약관 동의 처리
        if (request.getAgreements() != null) {
            addTermsAgreements(user, request.getAgreements());
        }

        // 회원가입 완료 처리 (Redis 정리, 토큰 생성)
        return completeKakaoSignup(user, request.getTempUserId(), kakaoUserInfo.getEmail());
    }

    // Redis에서 카카오 사용자 정보 조회 및 검증
    private KakaoUserInfo getKakaoUserInfoFromRedis(String tempUserId) {
        String redisKey = TEMP_USER_PREFIX + tempUserId;
        KakaoUserInfo kakaoUserInfo = (KakaoUserInfo) redisTemplate.opsForValue().get(redisKey);

        if (kakaoUserInfo == null) {
            throw new UserException(ErrorStatus.NOT_FOUND_USER);
        }

        return kakaoUserInfo;
    }

    // 카카오 사용자 프로필 이미지 및 닉네임 처리
    private void processKakaoUserProfile(User user, String email, KakaoSignupCompleteRequestDTO request) {
        // 프로필 이미지 URL 결정 및 적용 (Redis 임시 이미지 우선)
        String profileImgUrl = resolveProfileImageUrl(email, request.getProfileImg());
        if (profileImgUrl != null) {
            processProfileImage(user, profileImgUrl);
        }

        // 닉네임 처리
        if (request.getNickname() != null) {
            processNickname(user, request.getNickname());
        }
    }

    // 카카오 회원가입 완료 처리
    private SignupResponseDTO completeKakaoSignup(User user, String tempUserId, String email) {
        // Redis 데이터 삭제
        String redisKey = TEMP_USER_PREFIX + tempUserId;
        redisTemplate.delete(redisKey);
        
        // 임시 프로필 이미지 URL 삭제
        clearTempProfileImage(email);

        // JWT 토큰 생성 및 응답
        String accessToken = generateAccessToken(user);

        // 응답 DTO 생성 (converter 사용)
        return userConverter.toSignupResponseDTO(user, accessToken);
    }

    // 카카오 기본 사용자 생성
    private User createBasicKakaoUser(KakaoUserInfo kakaoUserInfo, KakaoSignupCompleteRequestDTO request) {
        // 필수 약관 동의 검증
        if (request.getAgreements() != null) {
            validateRequiredTerms(request.getAgreements());
        }

        // 프로필 이미지 URL 결정 (Redis 임시 이미지 우선)
        String profileImgUrl = resolveProfileImageUrl(kakaoUserInfo.getEmail(), request.getProfileImg());

        // User 생성 (converter 사용)
        User user = userConverter.toUserEntityFromKakao(kakaoUserInfo, request, profileImgUrl);
        User savedUser = userRepository.save(user);

        // OAuth 계정 연결 (converter 사용)
        OAuthAccount oAuthAccount = userConverter.toOAuthAccountEntity(savedUser, kakaoUserInfo.getEmail(), AuthProvideerEnum.KAKAO);
        oAuthAccountRepository.save(oAuthAccount);

        return savedUser;
    }

    // 프로필 이미지 처리
    private void processProfileImage(User user, String profileImg) {
        user.updateProfileImage(profileImg);
        userRepository.save(user);
    }

    // 닉네임 처리
    private void processNickname(User user, String nickname) {
        // 기존 updateNickname 로직 재사용
        // 현재 사용자가 이미 같은 닉네임을 사용하고 있는지 확인
        if (user.getNickname().equals(nickname)) {
            return;
        }

        if (userRepository.existsByNickname(nickname)) {
            throw new UserException(ErrorStatus.EXIST_NICKNAME);
        }

        user.setNickname(nickname);
        userRepository.save(user);
    }

    // 이메일 변경 인증 메일 발송
    @Override
    public EmailSendResponseDTO sendEmailChangeVerification(String currentEmail, String newEmail) {
        // 새 이메일이 이미 사용 중인지 확인
        checkEmail(newEmail);
        
        // 이메일 변경 인증 메일 발송
        String changeToken = emailService.sendEmailChangeVerificationEmail(currentEmail, newEmail);
        
        return createEmailSendResponse(newEmail, changeToken, "이메일 변경 확인 메일이 발송되었습니다");
    }

    // 이메일 변경 완료
    @Override
    public void completeEmailChange(String token) {
        String emailPair = emailService.validateEmailChangeToken(token);
        String[] emails = emailPair.split(":");
        String currentEmail = emails[0];
        String newEmail = emails[1];
        
        // 사용자 조회
        User user = userRepository.findByEmailAndUserActivate(currentEmail, UserActivate.ACTIVE)
                .orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_USER));
        
        // 새 이메일로 업데이트
        user.setEmail(newEmail);
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        
        // 기존 이메일 인증 정보 정리
        emailService.clearVerificationToken(currentEmail);
        
        // 이메일 변경 토큰 삭제 (완료 후)
        emailService.clearEmailChangeToken(token);
        
        userRepository.save(user);
    }

    // 이메일 변경 인증 메일 재발송
    @Override
    public EmailSendResponseDTO resendEmailChangeVerification(String currentEmail, String newEmail) {
        // 새 이메일이 이미 사용 중인지 확인
        checkEmail(newEmail);
        
        // 이메일 변경 인증 메일 재발송
        String changeToken = emailService.resendEmailChangeVerificationEmail(currentEmail, newEmail);
        
        return createEmailSendResponse(newEmail, changeToken, "이메일 변경 확인 메일이 재발송되었습니다");
    }

    // 닉네임 중복 확인
    @Override
    public EmailDuplicateResponseDTO checkNicknameDuplicate(String nickname) {
        boolean isAvailable = !userRepository.existsByNickname(nickname);
        
        String message = isAvailable ? 
            "사용 가능한 닉네임입니다." : 
            "이미 사용 중인 닉네임입니다.";
        
        return userConverter.toEmailDuplicateResponseDTO(isAvailable, message);
    }

    // 사용자 ID로 친구 목록 조회
    @Override
    public List<User> getFriendsByUserId(Long creatorId) {
        User user = getUserbyUserId(creatorId);
        List<Friend> friends = friendRepository.findByStatusAndUserIdOrStatusAndFriendIdOrderByCreatedAtDesc(
                FriendStatus.ACCEPTED, user.getId(), FriendStatus.ACCEPTED, user.getId()
        );        return friends.stream()
                .map(Friend::getFriend)
                .toList();
    }

    // 사용자 정보 기반 JWT 액세스 토큰 생성
    private String generateAccessToken(User user) {
        return jwtUtil.generateToken(user.getEmail(), user.getRole().toString(), user.getId());
    }

    // 프로필 이미지 URL 결정
    // 우선순위: Redis 임시 저장 이미지 > 요청에서 받은 이미지
    private String resolveProfileImageUrl(String email, String requestProfileImg) {
        // Redis에서 임시 저장된 프로필 이미지 URL 가져오기
        Object tempImageUrlObj = redisTemplate.opsForValue().get(TEMP_PROFILE_PREFIX + email);
        String tempImageUrl = (tempImageUrlObj != null) ? (String) tempImageUrlObj : null;
        
        // 요청에서 받은 프로필 이미지 처리 (빈 문자열은 null로 변환)
        String processedRequestImg = requestProfileImg;
        if (processedRequestImg != null && processedRequestImg.trim().isEmpty()) {
            processedRequestImg = null;
        }
        
        // 우선순위: Redis 임시 이미지 > 요청 이미지
        return (tempImageUrl != null) ? tempImageUrl : processedRequestImg;
    }

    // Redis에 저장된 임시 프로필 이미지 삭제
    private void clearTempProfileImage(String email) {
        String redisKey = TEMP_PROFILE_PREFIX + email;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            redisTemplate.delete(redisKey);
        }
    }

    // 이메일 발송 응답 DTO 생성
    private EmailSendResponseDTO createEmailSendResponse(String email, String verificationToken, String message) {
        return userConverter.toEmailSendResponseDTO(email, verificationToken, message);
    }

    // 이메일 인증 발송
    @Override
    public EmailSendResponseDTO sendEmailVerification(String email) {
        // 이메일 중복 체크
        checkEmail(email);

        // 인증 메일 발송
        String verificationToken = emailService.sendVerificationEmail(email);

        return userConverter.toEmailSendResponseDTO(email, verificationToken, "인증 메일이 발송되었습니다");
    }

    // 이메일 인증 재발송
    @Override
    public EmailSendResponseDTO resendEmailVerification(String email) {
        // 이미 인증된 이메일인지 확인
        if (emailService.isEmailVerified(email)) {
            throw new UserException(ErrorStatus.EMAIL_ALREADY_VERIFIED);
        }

        // 인증 메일 재발송
        String verificationToken = emailService.resendVerificationEmail(email);

        return userConverter.toEmailSendResponseDTO(email, verificationToken, "인증 메일이 재발송되었습니다");
    }

    // 이메일 인증 상태 확인
    @Override
    public EmailVerificationStatusResponseDTO getEmailVerificationStatus(String token) {
        String email = emailService.validateToken(token);
        boolean verified = emailService.isEmailVerified(email);

        return userConverter.toEmailVerificationStatusResponseDTO(email, verified);
    }

    // 이메일 링크 클릭 처리
    @Override
    public String handleEmailVerificationLink(String token) {
        String email = emailService.completeVerification(token);

        String deepLinkUrl = "planup://profile/setup?email=" +
                java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8) +
                "&verified=true&token=" + token +
                "&from=email_verification";

        return emailService.createSuccessHtml(email, deepLinkUrl);
    }

    // 비밀번호 변경 링크 클릭 처리
    @Override
    public String handlePasswordChangeLink(String token) {
        String[] tokenInfo = emailService.validatePasswordChangeToken(token);
        String email = tokenInfo[0]; 
        Boolean isLoggedIn = Boolean.parseBoolean(tokenInfo[1]);
        
        // 비밀번호 변경 이메일 인증 완료 표시
        emailService.markPasswordChangeEmailAsVerified(email);
        
        // 로그인 상태에 따른 딥링크 경로 분기
        String deepLinkUrl;
        if (isLoggedIn) {
            // 로그인한 상태: 마이페이지로 이동
            deepLinkUrl = "planup://mypage/password/change?email=" +
                    java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8) +
                    "&verified=true&token=" + token +
                    "&from=password_change&loggedIn=true";
        } else {
            // 로그인하지 않은 상태: 로그인 화면으로 이동
            deepLinkUrl = "planup://login/password/change?email=" +
                    java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8) +
                    "&verified=true&token=" + token +
                    "&from=password_change&loggedIn=false";
        }

        return emailService.createSuccessHtml(email, deepLinkUrl);
    }

    // 이메일 변경 링크 클릭 처리
    @Override
    public String handleEmailChangeLink(String token) {
        EmailVerifyLinkResponseDTO response = emailService.handleEmailChangeLink(token);
        
        if (response.isVerified()) {
            // 실제 이메일 변경 실행
            completeEmailChange(token);
            
            // 성공 시 HTML 페이지 표시
            String deepLinkUrl = "planup://email/change/complete?verified=true&token=" + token;
            return emailService.createSuccessHtml(response.getEmail(), deepLinkUrl);
        } else {
            // 실패 시 에러 HTML 페이지 표시
            return emailService.createFailureHtml();
        }
    }

    // 이메일 중복 확인
    @Override
    public EmailDuplicateResponseDTO checkEmailDuplicate(String email) {
        boolean isAvailable = isEmailAvailable(email);
        
        return userConverter.toEmailDuplicateResponseDTO(isAvailable, isAvailable ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.");
    }

    // 이메일 변경 인증 메일 발송 (userId 기반)
    @Override
    public EmailSendResponseDTO sendEmailChangeVerification(Long userId, String newEmail) {
        User currentUser = getUserbyUserId(userId);
        return sendEmailChangeVerification(currentUser.getEmail(), newEmail);
    }

    // 이메일 변경 인증 메일 재발송 (userId 기반)
    @Override
    public EmailSendResponseDTO resendEmailChangeVerification(Long userId, String newEmail) {
        User currentUser = getUserbyUserId(userId);
        return resendEmailChangeVerification(currentUser.getEmail(), newEmail);
    }
}
