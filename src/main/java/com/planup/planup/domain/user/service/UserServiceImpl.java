package com.planup.planup.domain.user.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.friend.entity.FriendStatus;
import com.planup.planup.domain.friend.repository.FriendRepository;
import com.planup.planup.domain.global.service.ImageUploadService;
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

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
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

        // 비밀번호 확인 검증
        if (!request.getPassword().equals(request.getPasswordCheck())) {
            throw new UserException(ErrorStatus.PASSWORD_MISMATCH);
        }

        // 필수 약관 동의 검증
        validateRequiredTerms(request.getAgreements());

        // 초대코드 처리 (있을 때만)
        Long inviterId = null;
        String friendNickname = null;
        if (request.getInviteCode() != null && !request.getInviteCode().trim().isEmpty()) {
            inviterId = inviteCodeService.findInviterByCode(request.getInviteCode());
            if (inviterId == null) {
                throw new UserException(ErrorStatus.INVALID_INVITE_CODE);
            }
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        // User 엔티티 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .role(Role.USER)
                .userActivate(UserActivate.ACTIVE)
                .userLevel(UserLevel.LEVEL_1)
                .alarmAllow(true)
                .profileImg(request.getProfileImg())
                .build();

        User savedUser = userRepository.save(user);

        // 약관 동의 추가
        addTermsAgreements(savedUser, request.getAgreements());

        // 초대 관계 처리 (초대코드가 있었다면)
        if (inviterId != null) {

            // 친구 관계 생성 (양방향)
            User inviterUser = getUserbyUserId(inviterId);
            friendNickname = inviterUser.getNickname();

            Friend friendship1 = Friend.builder()
                    .user(savedUser)
                    .friend(inviterUser)
                    .status(FriendStatus.ACCEPTED)
                    .build();

            Friend friendship2 = Friend.builder()
                    .user(inviterUser)
                    .friend(savedUser)
                    .status(FriendStatus.ACCEPTED)
                    .build();

            friendRepository.save(friendship1);
            friendRepository.save(friendship2);

            // 초대코드 사용 완료
            inviteCodeService.useInviteCode(request.getInviteCode());
        }

        return SignupResponseDTO.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .friendNickname(friendNickname)
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
        //  이메일로 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_USER));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserException(ErrorStatus.INVALID_CREDENTIALS);
        }

        // 사용자 상태 확인
        if (user.getUserActivate() != UserActivate.ACTIVE) {
            throw new UserException(ErrorStatus.USER_INACTIVE);
        }

        // 4. JWT 토큰 생성
        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().toString(), user.getId());

        // 응답 DTO 생성
        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .nickname(user.getNickname())
                .profileImgUrl(user.getProfileImg())
                .build();
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

            UserTerms saved = userTermsRepository.save(userTerms);
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
    public ImageUploadResponseDTO uploadProfileImage(MultipartFile file, User currentUser) {

        String imageUrl = imageUploadService.uploadImage(file, "profile");

        currentUser.updateProfileImage(imageUrl);
        userRepository.save(currentUser);

        return ImageUploadResponseDTO.builder()
                .imageUrl(imageUrl)
                .build();
    }

    // 내 초대코드 조회 메서드
    @Override
    public InviteCodeResponseDTO getMyInviteCode(Long userId) {
        return inviteCodeService.getMyInviteCode(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public ValidateInviteCodeResponseDTO validateInviteCode(String inviteCode, Long currentUserId) {
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

            // 본인 코드인지 확인
            if (inviterId.equals(currentUserId)) {
                return ValidateInviteCodeResponseDTO.builder()
                        .valid(false)
                        .message("본인의 초대코드는 사용할 수 없습니다.")
                        .build();
            }

            // 이미 친구인지 확인
            User currentUser = getUserbyUserId(currentUserId);
            User inviterUser = getUserbyUserId(inviterId);

            boolean alreadyFriend = friendRepository.findByUserAndFriend_NicknameAndStatus(
                    currentUser, inviterUser.getNickname(), FriendStatus.ACCEPTED).isPresent() ||
                    friendRepository.findByUserAndFriend_NicknameAndStatus(
                            inviterUser, currentUser.getNickname(), FriendStatus.ACCEPTED).isPresent();

            if (alreadyFriend) {
                return ValidateInviteCodeResponseDTO.builder()
                        .valid(false)
                        .message("이미 친구로 등록된 사용자입니다.")
                        .build();
            }

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
}
