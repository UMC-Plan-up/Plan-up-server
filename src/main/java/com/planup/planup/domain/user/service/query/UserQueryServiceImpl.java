package com.planup.planup.domain.user.service.query;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.user.converter.TermsConverter;
import com.planup.planup.domain.user.converter.UserAuthConverter;
import com.planup.planup.domain.user.converter.UserProfileConverter;
import com.planup.planup.domain.user.dto.AuthResponseDTO;
import com.planup.planup.domain.user.dto.OAuthResponseDTO;
import com.planup.planup.domain.user.dto.UserResponseDTO;
import com.planup.planup.domain.user.entity.*;
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
    private final UserProfileConverter userProfileConverter;

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
        return userAuthConverter.toUserInfoResponseDTO(user);
    }

    // ========== 이메일 검증 ==========

    @Override
    public void checkEmail(String email) {
        if (userRepository.existsByEmailAndUserActivate(email, UserActivate.ACTIVE)) {
            throw new UserException(ErrorStatus.USER_EMAIL_ALREADY_EXISTS);
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
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmailAndUserActivate(email, UserActivate.ACTIVE);
    }

    @Override
    public AuthResponseDTO.EmailDuplicate checkEmailDuplicate(String email) {
        boolean isAvailable = isEmailAvailable(email);
        return userAuthConverter.toEmailDuplicateResponseDTO(
                isAvailable,
                isAvailable ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다."
        );
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
        List<Adjective> adjectives = adjectiveRepository.findAll();
        List<Noun> nouns = nounRepository.findAll();

        if (adjectives.isEmpty() || nouns.isEmpty()) {
            log.error("형용사 또는 명사 데이터가 없습니다. adjectives: {}, nouns: {}", adjectives.size(), nouns.size());
            throw new UserException(ErrorStatus.NICKNAME_DATA_NOT_FOUND);
        }

        RandomNickname randomNickname = generateNickname(adjectives, nouns);

        return UserResponseDTO.RandomNickname.builder()
                .nickname(randomNickname.getFullNickname())
                .build();
    }

    private RandomNickname generateNickname(List<Adjective> adjectives, List<Noun> nouns) {
        int maxAttempts = 10;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Adjective adjective = adjectives.get(random.nextInt(adjectives.size()));
            Noun noun = nouns.get(random.nextInt(nouns.size()));

            String fullNickname = adjective.getWord() + noun.getWord();

            if (fullNickname.length() > 20) {
                continue;
            }

            if (!userRepository.existsByNickname(fullNickname)) {
                return RandomNickname.of(adjective.getWord(), noun.getWord());
            }
        }

        log.warn("랜덤 닉네임 생성 중 중복이 많아 기본 닉네임을 반환합니다.");
        return RandomNickname.of("행복한", "사용자");
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
        String email = getEmailByToken(token);
        boolean verified = isEmailVerified(email);
        return userAuthConverter.toEmailVerificationStatusResponseDTO(email, verified);
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

    // ========== 관계 조회 ==========

    @Override
    public List<User> getFriendsByUserId(Long userId) {
        User user = getUserByUserId(userId);
        List<Friend> friends = friendRepository.findByStatusAndUserIdOrStatusAndFriendIdOrderByCreatedAtDesc(
                FriendStatus.ACCEPTED, user.getId(), FriendStatus.ACCEPTED, user.getId()
        );
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

        redisTemplate.opsForValue().set(key, newCode, 3, TimeUnit.DAYS);
        redisTemplate.opsForValue().set("invite_code:" + newCode, userId.toString(), 3, TimeUnit.DAYS);

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
    public List<AuthResponseDTO.TermsList> getTermsList() {
        List<Terms> termsList = termsRepository.findAllByOrderByOrderAsc();
        return TermsConverter.toTermsListResponseList(termsList);
    }

    @Override
    public AuthResponseDTO.TermsDetail getTermsDetail(Long termsId) {
        Terms terms = termsRepository.findById(termsId)
                .orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_TERMS));
        return TermsConverter.toTermsDetailResponse(terms);
    }
}