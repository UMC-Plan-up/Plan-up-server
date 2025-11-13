package com.planup.planup.domain.user.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.friend.entity.FriendStatus;
import com.planup.planup.domain.friend.repository.FriendRepository;
import com.planup.planup.domain.global.service.ImageUploadService;
import com.planup.planup.domain.oauth.entity.OAuthAccount;
import com.planup.planup.domain.user.dto.*;
import com.planup.planup.domain.user.entity.*;
import com.planup.planup.domain.user.repository.InvitedUserRepository;
import com.planup.planup.domain.user.repository.TermsRepository;
import com.planup.planup.domain.user.repository.UserRepository;
import com.planup.planup.validation.jwt.JwtUtil;
import com.planup.planup.validation.jwt.dto.TokenResponseDTO;
import com.planup.planup.validation.jwt.service.TokenService;
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
    private final TokenService tokenService;
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

    @Qualifier("objectRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

    // Redis 키 prefix
    private static final String TEMP_USER_PREFIX = "temp_kakao_user:";
    private static final int TEMP_USER_EXPIRE_MINUTES = 60; // 60분 후 만료

    @Override
    @Transactional(readOnly = true)
    public User getUserbyUserId(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_USER));
    }

    @Override
    @Transactional(readOnly = true)
    public String getNickname(Long userId) {
        User user = getUserbyUserId(userId);
        return user.getNickname();
    }

    @Override
    @Transactional
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

    @Override
    @Transactional
    public boolean updateMarketingNotificationAllow(Long userId) {
        User user = getUserbyUserId(userId);
        return user.updateMarketingNotificationAllow();
    }

    @Override
    @Transactional
    public boolean updateServiceNotificationAllow(Long userId) {
        User user = getUserbyUserId(userId);
        return user.updateServiceNotificationAllow();
    }
    
    @Override
    public Boolean isPasswordChangeEmailVerified(String email) {
        return emailService.isPasswordChangeEmailVerified(email);
    }

    @Override
    @Transactional
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

    @Override
    @Transactional(readOnly = true)
    public UserInfoResponseDTO getUserInfo(Long userId) {
        User user = getUserbyUserId(userId);
        return UserInfoResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImg(user.getProfileImg())
                .serviceNotificationAllow(user.getServiceNotificationAllow())
                .marketingNotificationAllow(user.getMarketingNotificationAllow())
                .build();
    }

    @Override
    @Transactional
    public SignupResponseDTO signup(SignupRequestDTO request) {
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

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        // 임시 저장된 프로필 이미지 URL 가져오기
        Object tempImageUrlObj = redisTemplate.opsForValue().get("temp_profile:" + request.getEmail());
        String tempImageUrl = (tempImageUrlObj != null) ? (String) tempImageUrlObj : null;
        
        // 요청에서 받은 프로필 이미지 처리 (빈 문자열은 null로 변환)
        String requestProfileImg = request.getProfileImg();
        if (requestProfileImg != null && requestProfileImg.trim().isEmpty()) {
            requestProfileImg = null;
        }
        
        // 우선순위: Redis 임시 이미지 > 요청 이미지
        String profileImgUrl = (tempImageUrl != null) ? tempImageUrl : requestProfileImg;

        // User 엔티티 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .role(Role.USER)
                .userActivate(UserActivate.ACTIVE)
                .userLevel(UserLevel.LEVEL_1)
                .serviceNotificationAllow(true) // 서비스 알림 기본값: true
                .marketingNotificationAllow(true) // 혜택 및 마케팅 알림 기본값: true
                .profileImg(profileImgUrl)
                .emailVerified(true)
                .emailVerifiedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        // 약관 동의 추가
        addTermsAgreements(savedUser, request.getAgreements());

        // 토큰 생성 (TokenService 사용)
        TokenResponseDTO tokenResponse = tokenService.generateTokens(savedUser);

        // 인증 토큰 정리
        emailService.clearVerificationToken(request.getEmail());
        
        // 임시 프로필 이미지 URL 삭제
        if (tempImageUrl != null) {
            redisTemplate.delete("temp_profile:" + request.getEmail());
        }

        return SignupResponseDTO.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .expiresIn(tokenResponse.getExpiresIn())
                .build();
    }

    @Override
    @Transactional
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

    @Override
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        try {
            //  이메일로 사용자 조회
            User user = userRepository.findByEmailAndUserActivate(request.getEmail(), UserActivate.ACTIVE)
                    .orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_USER));

            // 비밀번호 검증
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new UserException(ErrorStatus.INVALID_CREDENTIALS);
            }

            // 토큰 생성 (TokenService 사용)
            TokenResponseDTO tokenResponse = tokenService.generateTokens(user);

            // 응답 DTO 생성
            return LoginResponseDTO.builder()
                    .accessToken(tokenResponse.getAccessToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .expiresIn(tokenResponse.getExpiresIn())
                    .nickname(user.getNickname())
                    .profileImgUrl(user.getProfileImg())
                    .message("로그인에 성공했습니다")
                    .build();
        }  catch (UserException e) {

            String errorMessage;
            if (e.getErrorStatus() == ErrorStatus.NOT_FOUND_USER) {
                errorMessage = "존재하지 않는 사용자입니다";
            } else if (e.getErrorStatus() == ErrorStatus.INVALID_CREDENTIALS) {
                errorMessage = "비밀번호가 일치하지 않습니다";
            } else if (e.getErrorStatus() == ErrorStatus.USER_INACTIVE) {
                errorMessage = "비활성화된 계정입니다";
            } else {
                errorMessage = "로그인에 실패했습니다";
            }

            return LoginResponseDTO.builder()
                    .message(errorMessage)
                    .build();
        }
    }

    @Override
    @Transactional
    public void logout(Long userId, jakarta.servlet.http.HttpServletRequest request) {
        // Authorization 헤더에서 액세스 토큰 추출
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = jwtUtil.extractTokenFromHeader(authHeader);
            if (accessToken != null) {
                // 액세스 토큰 블랙리스트 추가
                tokenService.blacklistAccessToken(accessToken);
            }
        }
        
        // 리프레시 토큰 삭제
        tokenService.logout(userId);
        
        log.info("로그아웃 완료 - 사용자 ID: {}", userId);
    }

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

    private void addTermsAgreements(User user, List<TermsAgreementRequestDTO> agreements) {

        for (TermsAgreementRequestDTO agreement : agreements) {

            Terms terms = termsRepository.findById(agreement.getTermsId())
                    .orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_TERMS));

            UserTerms userTerms = UserTerms.builder()
                    .user(user)
                    .terms(terms)
                    .isAgreed(agreement.isAgreed())
                    .agreedAt(agreement.isAgreed() ? LocalDateTime.now() : null)
                    .build();

            userTermsRepository.save(userTerms);
        }
    }
      
    @Override
    @Transactional(readOnly = true)
    public KakaoAccountResponseDTO getKakaoAccountStatus(Long userId) {
        User user = getUserbyUserId(userId);
        
        // 카카오톡 계정 정보 조회 (한 번에 조회)
        var oauthAccount = oAuthAccountRepository.findByUserAndProvider(user, AuthProvideerEnum.KAKAO);
        
        boolean isLinked = oauthAccount.isPresent();
        String kakaoEmail = oauthAccount.map(account -> account.getEmail()).orElse(null);
        
        return KakaoAccountResponseDTO.builder()
                .isLinked(isLinked)
                .kakaoEmail(kakaoEmail)
                .build();
    }

    @Override
    @Transactional
    public ImageUploadResponseDTO uploadProfileImage(MultipartFile file, String email) {
        // 이미지 업로드
        String imageUrl = imageUploadService.uploadImage(file, "profile");
        
        // Redis에 임시 저장 (1시간 TTL)
        String redisKey = "temp_profile:" + email;
        redisTemplate.opsForValue().set(redisKey, imageUrl, Duration.ofHours(1));
        
        return ImageUploadResponseDTO.builder()
                .imageUrl(imageUrl)
                .build();
    }

    @Override
    @Transactional
    public ImageUploadResponseDTO updateProfileImage(Long userId, MultipartFile file) {
        // 사용자 조회
        User user = getUserbyUserId(userId);
        
        // 기존 프로필 이미지가 있다면 S3에서 삭제
        if (user.getProfileImg() != null && !user.getProfileImg().trim().isEmpty()) {
            imageUploadService.deleteImage(user.getProfileImg());
        }
        
        // 새 이미지 업로드
        String newImageUrl = imageUploadService.uploadImage(file, "profile");
        
        // 사용자 프로필 이미지 업데이트
        user.updateProfileImage(newImageUrl);
        userRepository.save(user);
        
        return ImageUploadResponseDTO.builder()
                .imageUrl(newImageUrl)
                .build();
    }

    // 내 초대코드 조회 메서드
    @Override
    public InviteCodeResponseDTO getMyInviteCode(Long userId) {
        return inviteCodeService.getMyInviteCode(userId);
    }

    @Override
    @Transactional
    public InviteCodeProcessResponseDTO processInviteCode(String inviteCode, Long userId) {
        // 빈 코드 체크
        if (inviteCode == null || inviteCode.trim().isEmpty()) {
            return InviteCodeProcessResponseDTO.builder()
                    .success(false)
                    .message("초대코드를 입력해주세요.")
                    .build();
        }

        try {
            // InviteCodeService를 통해 초대자 찾기
            Long inviterId = inviteCodeService.findInviterByCode(inviteCode);

            if (inviterId == null) {
                return InviteCodeProcessResponseDTO.builder()
                        .success(false)
                        .message("유효하지 않은 초대코드입니다.")
                        .build();
            }

            // 본인 코드인지 확인
            if (inviterId.equals(userId)) {
                return InviteCodeProcessResponseDTO.builder()
                        .success(false)
                        .message("자신의 초대코드는 사용할 수 없습니다.")
                        .build();
            }

            // 이미 친구인지 확인
            User currentUser = getUserbyUserId(userId);
            User inviterUser = getUserbyUserId(inviterId);
            
            boolean alreadyFriend = friendRepository.findByUserAndFriend_NicknameAndStatus(
                    currentUser, inviterUser.getNickname(), FriendStatus.ACCEPTED).isPresent() ||
                    friendRepository.findByUserAndFriend_NicknameAndStatus(
                            inviterUser, currentUser.getNickname(), FriendStatus.ACCEPTED).isPresent();

            if (alreadyFriend) {
                return InviteCodeProcessResponseDTO.builder()
                        .success(false)
                        .message("이미 친구인 사용자입니다.")
                        .build();
            }

            // 친구 관계 생성 (양방향)
            Friend friendship1 = Friend.builder()
                    .user(currentUser)
                    .friend(inviterUser)
                    .status(FriendStatus.ACCEPTED)
                    .build();

            Friend friendship2 = Friend.builder()
                    .user(inviterUser)
                    .friend(currentUser)
                    .status(FriendStatus.ACCEPTED)
                    .build();

            friendRepository.save(friendship1);
            friendRepository.save(friendship2);

            return InviteCodeProcessResponseDTO.builder()
                    .success(true)
                    .friendNickname(inviterUser.getNickname())
                    .message("친구 관계가 성공적으로 생성되었습니다.")
                    .build();

        } catch (Exception e) {
            return InviteCodeProcessResponseDTO.builder()
                    .success(false)
                    .message("초대코드 처리 중 오류가 발생했습니다.")
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ValidateInviteCodeResponseDTO validateInviteCode(String inviteCode) {
        // 빈 코드 체크
        if (inviteCode == null || inviteCode.trim().isEmpty()) {
            return ValidateInviteCodeResponseDTO.builder()
                    .valid(false)
                    .message("초대코드를 입력해주세요.")
                    .build();
        }

        try {
            // InviteCodeService를 통해 초대자 찾기
            Long inviterId = inviteCodeService.findInviterByCode(inviteCode);

            if (inviterId == null) {
                return ValidateInviteCodeResponseDTO.builder()
                        .valid(false)
                        .message("유효하지 않은 초대코드입니다.")
                        .build();
            }

            // 기본적인 유효성 검증 (초대코드 존재 여부, 초대자 정보)
            User inviterUser = getUserbyUserId(inviterId);

            return ValidateInviteCodeResponseDTO.builder()
                    .valid(true)
                    .message("유효한 초대코드입니다.")
                    .targetUserNickname(inviterUser.getNickname())
                    .build();

        } catch (Exception e) {
            return ValidateInviteCodeResponseDTO.builder()
                    .valid(false)
                    .message("초대코드 검증 중 오류가 발생했습니다.")
                    .build();
        }
    }

    @Override
    @Transactional
    public WithdrawalResponseDTO withdrawUser(Long userId, WithdrawalRequestDTO request) {
        // 사용자 조회
        User user = getUserbyUserId(userId);

        // 탈퇴 정보 저장
        UserWithdrawal withdrawal = UserWithdrawal.builder()
                .user(user)
                .reason(request.getReason())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();

        userWithdrawalRepository.save(withdrawal);

        // 사용자 상태를 비활성화로 변경
        user.setUserActivate(UserActivate.INACTIVE);
        userRepository.save(user);

        // 관련 데이터 정리 (선택사항)
        cleanupUserData(user);

        log.info("사용자 {} 회원 탈퇴 완료. 이유: {}", user.getNickname(), request.getReason());

        return WithdrawalResponseDTO.builder()
                .success(true)
                .message("회원 탈퇴가 완료되었습니다.")
                .withdrawalDate(LocalDateTime.now().toString())
                .build();
    }

    /**
     * 사용자 관련 데이터 정리
     */
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

    /**
     * 회원가입 시 이메일 중복 체크
     * - 이미 존재하는 활성 사용자면 예외 발생
     */
    @Override
    @Transactional(readOnly = true)
    public void checkEmail(String email){
        if (userRepository.existsByEmailAndUserActivate(email, UserActivate.ACTIVE)) {
            throw new UserException(ErrorStatus.USER_EMAIL_ALREADY_EXISTS);
        }
    }

    /**
     * 비밀번호 변경 시 이메일 존재 여부 체크
     * - 존재하지 않거나 비활성 사용자면 예외 발생
     */
    @Override
    @Transactional(readOnly = true)
    public void checkEmailExists(String email) {
        Optional<User> user = userRepository.findByEmailAndUserActivate(email, UserActivate.ACTIVE);
        if (user.isEmpty()) {
            throw new UserException(ErrorStatus.NOT_FOUND_USER);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        try {
            checkEmail(email);
            return true;
        } catch (UserException e) {
            return false;
        }
    }

    /**
     * 비밀번호 변경 이메일 발송
     * - 이메일 존재 여부 확인 후 인증 메일 발송
     */
    @Override
    @Transactional
    public EmailSendResponseDTO sendPasswordChangeEmail(String email, Boolean isLoggedIn) {
        // 이메일이 등록된 사용자인지 확인
        checkEmailExists(email);

        // 비밀번호 변경 이메일 발송 (로그인 상태 정보 포함)
        String changeToken = emailService.sendPasswordChangeEmail(email, isLoggedIn);

        // 응답 DTO 생성
        return EmailSendResponseDTO.builder()
                .email(email)
                .message("비밀번호 변경 확인 메일이 발송되었습니다")
                .verificationToken(changeToken)
                .build();
    }

    /**
     * 비밀번호 변경 이메일 재발송
     * - 이메일 존재 여부 확인 후 인증 메일 재발송
     */
    @Override
    @Transactional
    public EmailSendResponseDTO resendPasswordChangeEmail(String email, Boolean isLoggedIn) {
        // 이메일이 등록된 사용자인지 확인
        checkEmailExists(email);
    
        // 비밀번호 변경 이메일 재발송 (로그인 상태 포함)
        String changeToken = emailService.resendPasswordChangeEmail(email, isLoggedIn);
    
        return EmailSendResponseDTO.builder()
                .email(email)
                .message("비밀번호 변경 확인 메일이 재발송되었습니다")
                .verificationToken(changeToken)
                .build();
    }

    @Override
    public KakaoAuthResponseDTO kakaoAuth(KakaoAuthRequestDTO request) {
        KakaoUserInfo kakaoUserInfo = kakaoApiService.getUserInfo(request.getCode());
        String email = kakaoUserInfo.getEmail();
        return handleKakaoAuth(kakaoUserInfo, email);
    }

    @Override
    @Transactional
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
        
        // OAuth 계정 생성 및 연결
        OAuthAccount oAuthAccount = OAuthAccount.builder()
                .provider(AuthProvideerEnum.KAKAO)
                .email(email)
                .user(user)
                .build();
        
        oAuthAccountRepository.save(oAuthAccount);
        
        // 연동 성공 응답
        return KakaoLinkResponseDTO.builder()
                .success(true)
                .message("카카오 계정 연동이 완료되었습니다")
                .kakaoEmail(email)
                .userInfo(UserInfoResponseDTO.from(user))
                .build();
    }
    // 기존 카카오 로그인/회원가입 처리
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

            // 토큰 생성 (TokenService 사용)
            TokenResponseDTO tokenResponse = tokenService.generateTokens(user);

            KakaoAuthResponseDTO response = KakaoAuthResponseDTO.builder()
            .isNewUser(false)
            .accessToken(tokenResponse.getAccessToken())
            .refreshToken(tokenResponse.getRefreshToken())
            .expiresIn(tokenResponse.getExpiresIn())
            .userInfo(UserInfoResponseDTO.from(user))
            .build();
        return response;
        } else {
            // 신규 사용자 - Redis에 카카오 정보만 저장
            String tempUserId = UUID.randomUUID().toString();
            String redisKey = TEMP_USER_PREFIX + tempUserId;

            redisTemplate.opsForValue().set(redisKey, kakaoUserInfo, Duration.ofMinutes(TEMP_USER_EXPIRE_MINUTES));

            KakaoAuthResponseDTO response = KakaoAuthResponseDTO.builder()
                    .isNewUser(true)
                    .tempUserId(tempUserId)
                    .build();
            return response;
        }
    }

    @Override
    @Transactional
    public SignupResponseDTO kakaoSignupComplete(KakaoSignupCompleteRequestDTO request) {
        // Redis에서 카카오 정보 조회
        String redisKey = TEMP_USER_PREFIX + request.getTempUserId();
        KakaoUserInfo kakaoUserInfo = (KakaoUserInfo) redisTemplate.opsForValue().get(redisKey);

        if (kakaoUserInfo == null) {
            throw new UserException(ErrorStatus.NOT_FOUND_USER);
        }

        // 기본 사용자 생성
        User user = createBasicKakaoUser(kakaoUserInfo, request);

        // 임시 저장된 프로필 이미지 URL 가져오기
        Object tempImageUrlObj = redisTemplate.opsForValue().get("temp_profile:" + kakaoUserInfo.getEmail());
        String tempImageUrl = (tempImageUrlObj != null) ? (String) tempImageUrlObj : null;
        
        // 요청에서 받은 프로필 이미지 처리 (빈 문자열은 null로 변환)
        String requestProfileImg = request.getProfileImg();
        if (requestProfileImg != null && requestProfileImg.trim().isEmpty()) {
            requestProfileImg = null;
        }
        
        // 프로필 이미지 처리 (임시 저장된 이미지 우선, 없으면 요청에서 받은 이미지 사용)
        String profileImgUrl = (tempImageUrl != null) ? tempImageUrl : requestProfileImg;
        if (profileImgUrl != null) {
            processProfileImage(user, profileImgUrl);
        }

        // 닉네임 처리
        if (request.getNickname() != null) {
            processNickname(user, request.getNickname());
        }

        // 초대코드 처리는 별도 API로 분리 (카카오 회원가입에서는 제거)

        // 약관 동의 처리
        if (request.getAgreements() != null) {
            addTermsAgreements(user, request.getAgreements());
        }

        // Redis 데이터 삭제
        redisTemplate.delete(redisKey);
        
        // 임시 프로필 이미지 URL 삭제
        if (tempImageUrl != null) {
            redisTemplate.delete("temp_profile:" + kakaoUserInfo.getEmail());
        }

        // 토큰 생성 (TokenService 사용)
        TokenResponseDTO tokenResponse = tokenService.generateTokens(user);

        return SignupResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .expiresIn(tokenResponse.getExpiresIn())
                .build();
    }

    // 카카오 기본 사용자 생성
    private User createBasicKakaoUser(KakaoUserInfo kakaoUserInfo, KakaoSignupCompleteRequestDTO request) {
        // 필수 약관 동의 검증
        if (request.getAgreements() != null) {
            validateRequiredTerms(request.getAgreements());
        }

        // User 생성
        User user = User.builder()
                .email(kakaoUserInfo.getEmail())
                .password(null) // 카카오는 비밀번호 없음
                .nickname(request.getNickname())
                .role(Role.USER)
                .userActivate(UserActivate.ACTIVE)
                .userLevel(UserLevel.LEVEL_1)
                .serviceNotificationAllow(true) // 서비스 알림 기본값: true
                .marketingNotificationAllow(true) // 혜택 및 마케팅 알림 기본값: true
                .profileImg(request.getProfileImg())
                .emailVerified(true)
                .emailVerifiedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        // OAuth 계정 연결
        OAuthAccount oAuthAccount = OAuthAccount.builder()
                .provider(AuthProvideerEnum.KAKAO)
                .email(kakaoUserInfo.getEmail())
                .user(savedUser)
                .build();
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




    @Override
    @Transactional
    public EmailSendResponseDTO sendEmailChangeVerification(String currentEmail, String newEmail) {
        // 새 이메일이 이미 사용 중인지 확인
        checkEmail(newEmail);
        
        // 이메일 변경 인증 메일 발송
        String changeToken = emailService.sendEmailChangeVerificationEmail(currentEmail, newEmail);
        
        return EmailSendResponseDTO.builder()
                .email(newEmail)
                .message("이메일 변경 확인 메일이 발송되었습니다")
                .verificationToken(changeToken)
                .build();
    }

    @Override
    @Transactional
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

    @Override
    @Transactional
    public EmailSendResponseDTO resendEmailChangeVerification(String currentEmail, String newEmail) {
        // 새 이메일이 이미 사용 중인지 확인
        checkEmail(newEmail);
        
        // 이메일 변경 인증 메일 재발송
        String changeToken = emailService.resendEmailChangeVerificationEmail(currentEmail, newEmail);
        
        return EmailSendResponseDTO.builder()
                .email(newEmail)
                .message("이메일 변경 확인 메일이 재발송되었습니다")
                .verificationToken(changeToken)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EmailDuplicateResponseDTO checkNicknameDuplicate(String nickname) {
        boolean isAvailable = !userRepository.existsByNickname(nickname);
        
        String message = isAvailable ? 
            "사용 가능한 닉네임입니다." : 
            "이미 사용 중인 닉네임입니다.";
        
        return EmailDuplicateResponseDTO.builder()
                .available(isAvailable)
                .message(message)
                .build();
    }

    @Override
    public List<User> getFriendsByUserId(Long creatorId) {
        User user = getUserbyUserId(creatorId);
        List<Friend> friends = friendRepository.findByStatusAndUserIdOrStatusAndFriendIdOrderByCreatedAtDesc(
                FriendStatus.ACCEPTED, user.getId(), FriendStatus.ACCEPTED, user.getId()
        );        return friends.stream()
                .map(Friend::getFriend)
                .toList();
    }
}
