package com.planup.planup.domain.user.service.query;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.AuthException;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.apiPayload.exception.custom.UserSuspendedException;
import com.planup.planup.domain.user.converter.TermsConverter;
import com.planup.planup.domain.user.converter.UserAuthConverter;
import com.planup.planup.domain.user.converter.UserProfileConverter;
import com.planup.planup.domain.user.dto.AuthResponseDTO;
import com.planup.planup.domain.user.dto.OAuthResponseDTO;
import com.planup.planup.domain.user.dto.UserResponseDTO;
import com.planup.planup.domain.user.entity.*;
import com.planup.planup.domain.user.enums.TokenStatus;
import com.planup.planup.domain.user.enums.UserActivate;
import com.planup.planup.domain.user.repository.*;
import com.planup.planup.domain.oauth.repository.OAuthAccountRepository;
import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.friend.entity.FriendStatus;
import com.planup.planup.domain.oauth.entity.AuthProvideerEnum;
import com.planup.planup.domain.friend.repository.FriendRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final AdjectiveRepository adjectiveRepository;
    private final NounRepository nounRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final FriendRepository friendRepository;
    private final TermsRepository termsRepository;
    private final Random random = new Random();
    private final UserAuthConverter userAuthConverter;

    // ========== 기본 정보 조회 ==========

    @Override
    public User getUserByUserId(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_USER));
    }

    @Override
    public String getNickname(Long userId) {
        User user = getUserByUserId(userId);
        return user.getNickname();
    }

    @Override
    public UserResponseDTO.UserInfo getUserInfo(Long userId) {
        User user = getUserByUserId(userId);
        return userAuthConverter.toUserInfo(user);
    }

    // ========== 이메일 검증 ==========

    @Override
    public void checkEmail(String email) {
        if (userRepository.existsByEmailAndUserActivate(email, UserActivate.ACTIVE)) {
            throw new UserException(ErrorStatus.USER_EMAIL_ALREADY_EXISTS);
        }
        Optional<User> deletedUser = userRepository.findByEmailAndUserActivate(email, UserActivate.DELETED);
        if (deletedUser.isPresent()) {
            LocalDateTime unblockAt = deletedUser.get().getSanctionEndAt();
            if (unblockAt != null && LocalDateTime.now().isBefore(unblockAt)) {
                throw new UserException(ErrorStatus.EMAIL_BLOCKED_BY_SANCTION);
            }
        }
    }

    @Override
    public void checkEmailExists(String email) {
        Optional<User> user = userRepository.findByEmailAndUserActivate(email, UserActivate.ACTIVE);
        if (user.isEmpty()) {
            throw new UserException(ErrorStatus.NOT_FOUND_USER);
        }
    }

    @Override
    public AuthResponseDTO.EmailDuplicate checkEmailDuplicate(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (user.getUserActivate() == UserActivate.DELETED) {
                LocalDateTime unblockAt = user.getSanctionEndAt();
                if (unblockAt != null && LocalDateTime.now().isBefore(unblockAt)) {
                    throw new UserSuspendedException(
                            ErrorStatus.USER_SANCTIONED_DELETED,
                            "DELETED",
                            user.getSanctionEndAt(),
                            user.getSanctionReason()
                    );
                }
            }

            if (user.getUserActivate() == UserActivate.SUSPENDED) {
                user.liftSuspensionIfExpired();
                if (user.getUserActivate() == UserActivate.SUSPENDED) {
                    throw new UserSuspendedException(
                            ErrorStatus.USER_SUSPENDED,
                            "SUSPENDED",
                            user.getSanctionEndAt(),
                            user.getSanctionReason()
                    );
                }
            }

            if (user.getUserActivate() == UserActivate.ACTIVE) {
                return userAuthConverter.toEmailDuplicateResponseDTO(false, "이미 사용 중인 이메일입니다.");
            }
        }

        return userAuthConverter.toEmailDuplicateResponseDTO(true, "사용 가능한 이메일입니다.");
    }

    // ========== 닉네임 ==========

    @Override
    public AuthResponseDTO.EmailDuplicate checkNicknameDuplicate(String nickname) {
        boolean isAvailable = !userRepository.existsByNickname(nickname);
        String message = isAvailable ?
                "사용 가능한 닉네임입니다." :
                "이미 사용 중인 닉네임입니다.";
        return userAuthConverter.toEmailDuplicateResponseDTO(isAvailable, message);
    }

    @Override
    public UserResponseDTO.RandomNickname generateRandomNickname() {
        Adjective adjective = adjectiveRepository.findRandomAdjective()
                .orElseThrow(() -> new UserException(ErrorStatus.NICKNAME_DATA_NOT_FOUND));
        Noun noun = nounRepository.findRandomNoun()
                .orElseThrow(() -> new UserException(ErrorStatus.NICKNAME_DATA_NOT_FOUND));

        String baseNickname = adjective.getWord() + noun.getWord();

        if (!userRepository.existsByNickname(baseNickname)) {
            return UserProfileConverter.toRandomNicknameResponseDTO(baseNickname);
        }

        // 중복이면 숫자 suffix 붙여서 재시도
        for (int i = 1; i < 1000 ; i++) {
            String candidate = baseNickname + i;

            if (!userRepository.existsByNickname(candidate)) {
                return UserProfileConverter.toRandomNicknameResponseDTO(candidate);
            }
        }
        throw new UserException(ErrorStatus.NICKNAME_GENERATION_FAILED);
    }

    // ========== 인증 상태 조회 ==========

    @Override
    public OAuthResponseDTO.KakaoAccount getKakaoAccountStatus(Long userId) {
        User user = getUserByUserId(userId);

        var oauthAccount = oAuthAccountRepository.findByUserAndProvider(user, AuthProvideerEnum.KAKAO);

        boolean isLinked = oauthAccount.isPresent();
        String kakaoEmail = oauthAccount.map(account -> account.getEmail()).orElse(null);

        return userAuthConverter.toKakaoAccountResponseDTO(isLinked, kakaoEmail);
    }

    @Override
    public Boolean isEmailVerified(String email) {
        String verified = redisTemplate.opsForValue().get("email-verified:" + email);
        return "VERIFIED".equals(verified);
    }

    @Override
    public Boolean isPasswordChangeEmailVerified(String email) {
        String verified = redisTemplate.opsForValue().get("password-change-verified:" + email);
        return "VERIFIED".equals(verified);
    }

    @Override
    public AuthResponseDTO.EmailVerificationStatus getEmailVerificationStatus(String token) {
        String tokenValue = redisTemplate.opsForValue().get("email-verification:" + token);
        
        if (tokenValue == null) {
            // 토큰이 없거나 만료된 경우
            return userAuthConverter.toEmailVerificationStatusResponseDTO(null, false, TokenStatus.EXPIRED_OR_INVALID);
        }
        
        // 토큰 값에서 이메일과 인증 상태 추출
        boolean verified = tokenValue.startsWith("VERIFIED:");
        String email = verified ? tokenValue.substring(9) : tokenValue; // "VERIFIED:" 제거
        
        // 인증 완료된 경우 verified: true, VALID 반환
        // 인증 미완료인 경우 verified: false, VALID 반환
        TokenStatus tokenStatus = TokenStatus.VALID;
        
        return userAuthConverter.toEmailVerificationStatusResponseDTO(email, verified, tokenStatus);
    }

    @Override
    public OAuthResponseDTO.KakaoLinkStatus getKakaoLinkStatus(Long userId) {
        User user = getUserByUserId(userId);
        boolean isLinked = oAuthAccountRepository.existsByUserAndProvider(user, AuthProvideerEnum.KAKAO);
        return userAuthConverter.toKakaoLinkStatusResponseDTO(isLinked);
    }

    private String getEmailByToken(String token) {
        String tokenValue = redisTemplate.opsForValue().get("email-verification:" + token);
        if (tokenValue == null) {
            throw new AuthException(ErrorStatus.INVALID_EMAIL_TOKEN);
        }
        // VERIFIED: 접두사가 있으면 제거하고 이메일만 반환
        return tokenValue.startsWith("VERIFIED:") ? tokenValue.substring(9) : tokenValue;
    }

    // ========== 관계 조회 ==========

    @Override
    public List<User> getFriendsByUserId(Long userId) {
        User user = getUserByUserId(userId);
        List<Friend> friends = friendRepository.findFriendsOfUser(FriendStatus.ACCEPTED, user.getId());

        return friends.stream()
                .map(Friend::getFriend)
                .toList();
    }

    @Override
    public AuthResponseDTO.InviteCode getMyInviteCode(Long userId) {
        String key = "user:" + userId + ":invite_code";
        String existingCode = redisTemplate.opsForValue().get(key);

        if (existingCode != null) {
            return AuthResponseDTO.InviteCode.of(existingCode);
        }

        String newCode = generateInviteCode();

        try {
            redisTemplate.opsForValue().set(key, newCode, 3, TimeUnit.DAYS);
            redisTemplate.opsForValue().set("invite_code:" + newCode, userId.toString(), 3, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("초대 코드 Redis 저장 실패: {}", e.getMessage(), e);
            throw new AuthException(ErrorStatus.REDIS_SAVE_FAILED);
        }

        return AuthResponseDTO.InviteCode.of(newCode);
    }

    @Override
    public AuthResponseDTO.ValidateInviteCode validateInviteCode(String inviteCode) {
        if (inviteCode == null || inviteCode.trim().isEmpty()) {
            throw new UserException(ErrorStatus.INVALID_INVITE_CODE);
        }

        Long inviterId = findInviterByCode(inviteCode);

        if (inviterId == null) {
            throw new UserException(ErrorStatus.INVALID_INVITE_CODE);
        }

        User inviterUser = getUserByUserId(inviterId);

        return userAuthConverter.toValidateInviteCodeResponseDTO(true, inviterUser.getNickname());
    }

    private Long findInviterByCode(String inviteCode) {
        String inviterIdStr = redisTemplate.opsForValue().get("invite_code:" + inviteCode);

        if (inviterIdStr != null) {
            return Long.parseLong(inviterIdStr);
        }

        return null;
    }

    private String generateInviteCode() {
        Random random = new Random();
        String code = String.format("%06d", random.nextInt(1000000));

        while (Boolean.TRUE.equals(redisTemplate.hasKey("invite_code:" + code))) {
            code = String.format("%06d", random.nextInt(1000000));
        }
        return code;
    }

    // ========== 약관 조회 ==========

    @Override
    public AuthResponseDTO.TermsList getTermsList() {
        List<Terms> termsList = termsRepository.findAllByOrderByOrderAsc();
        return TermsConverter.toTermsListResponse(termsList);
    }

    @Override
    public AuthResponseDTO.TermsDetail getTermsDetail(Long termsId) {
        Terms terms = termsRepository.findById(termsId)
                .orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_TERMS));
        return TermsConverter.toTermsDetailResponse(terms);
    }
}