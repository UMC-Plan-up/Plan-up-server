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
import com.planup.planup.validation.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    @Override
    public User getUserbyUserId(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_USER));
    }

    @Override
    public String getNickname(Long userId) {
        User user = getUserbyUserId(userId);
        return user.getNickname();
    }

    @Override
    @Transactional
    public String updateNickname(Long userId, String nickname) {
        User user = getUserbyUserId(userId);

        if (!userRepository.existsByNickname(nickname)) {
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

        if (passwordEncoder.matches(password, user.getPassword())) {
            return true;
        } else {
            return false;
        }
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
        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().toString(), user.getId());

        // 5. 응답 DTO 생성
        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .nickname(user.getNickname())
                .profileImgUrl(user.getProfileImg())
                .build();
    }

}
