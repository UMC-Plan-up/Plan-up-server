package com.planup.planup.domain.user.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.user.dto.LoginRequestDTO;
import com.planup.planup.domain.user.dto.LoginResponseDTO;
import com.planup.planup.domain.user.dto.SignupRequestDTO;
import com.planup.planup.domain.user.dto.SignupResponseDTO;
import com.planup.planup.domain.user.entity.Role;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.entity.UserActivate;
import com.planup.planup.domain.user.entity.UserLevel;
import com.planup.planup.domain.user.repository.UserRepository;
import com.planup.planup.domain.user.dto.UserInfoResponseDTO;
import com.planup.planup.domain.oauth.entity.AuthProvideerEnum;
import com.planup.planup.domain.oauth.repository.OAuthAccountRepository;
import com.planup.planup.validation.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.planup.planup.domain.user.dto.KakaoAccountResponseDTO;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OAuthAccountRepository oAuthAccountRepository;


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
    public boolean updateNotificationAgree(Long userId) {
        User user = getUserbyUserId(userId);
        user.switchAlarmAllow();
        return true;
    }

    @Override
    @Transactional
    public boolean checkPassword(Long userId, String password) {
        User user = getUserbyUserId(userId);

        return passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String password) {
        User user = getUserbyUserId(userId);

        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
    }

    @Override
    @Transactional
    public String updateProfileImage(Long userId, MultipartFile imageFile) {
        User user = getUserbyUserId(userId);

        // 파일 저장 경로 설정 (예: /uploads/profile/)
        String uploadDir = "/uploads/profile/";
        String fileName = userId + "_" + imageFile.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + fileName);

        try {
            Files.createDirectories(filePath.getParent());
            imageFile.transferTo(filePath.toFile());
            // DB에 경로 저장
            user.setProfileImg(filePath.toString());
            // userRepository.save(user); // 필요시 저장
            return filePath.toString();
        } catch (Exception e) {
            throw new RuntimeException("프로필 이미지 저장 실패", e);
        }
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
                .build();
    }

    @Override
    @Transactional
    public SignupResponseDTO signup(SignupRequestDTO request) {
        // 1. 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserException(ErrorStatus.USER_EMAIL_ALREADY_EXISTS);
        }

        // 2. 비밀번호 확인 검증
        if (!request.getPassword().equals(request.getPasswordCheck())) {
            throw new UserException(ErrorStatus.PASSWORD_MISMATCH);
        }

        // 3. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 4. User 엔티티 생성 및 저장
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .role(Role.USER)  // 기본 역할
                .userActivate(UserActivate.ACTIVE)  // 활성 상태
                .userLevel(UserLevel.LEVEL_1)  // 기본 레벨
                .alarmAllow(true)  // 기본 알림 허용
                .profileImg(request.getProfileImg())
                .build();

        User savedUser = userRepository.save(user);

        // 5. 약관 동의 처리 (추후 구현)
        // TODO: 약관 동의 저장 로직 구현 필요

        // 6. 응답 DTO 생성
        return SignupResponseDTO.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
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
        if (userRepository.existsByEmail(newEmail)) {
            throw new UserException(ErrorStatus.EXIST_EMAIL);
        }

        user.setEmail(newEmail);
        return user.getEmail();
    }

    @Override
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        // 1. 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_USER));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserException(ErrorStatus.INVALID_CREDENTIALS);
        }

        // 3. 사용자 상태 확인
        if (user.getUserActivate() != UserActivate.ACTIVE) {
            throw new UserException(ErrorStatus.USER_INACTIVE);
        }

        // 4. JWT 토큰 생성
        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().toString());

        // 5. 응답 DTO 생성
        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .nickname(user.getNickname())
                .profileImgUrl(user.getProfileImg())
                .build();
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
}
