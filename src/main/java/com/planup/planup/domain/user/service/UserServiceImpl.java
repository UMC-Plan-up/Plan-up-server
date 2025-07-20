package com.planup.planup.domain.user.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.repository.UserRepository;
import com.planup.planup.domain.user.dto.UserInfoResponseDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;


@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;


    @Override
    public User getUserbyUserId(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        User user = userOptional.orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_USER));
        return user;
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

        //TODO: password 인코딩 해야 함

        if (user.getPassword().equals(password)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String password) {
        User user = getUserbyUserId(userId);

        //TODO: password 인코딩 해야 함

        user.setPassword(user.getPassword());
    }

    @Override
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
    public UserInfoResponseDTO getUserInfo(Long userId) {
        User user = getUserbyUserId(userId);
        return UserInfoResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImg(user.getProfileImg())
                .build();
    }
}
