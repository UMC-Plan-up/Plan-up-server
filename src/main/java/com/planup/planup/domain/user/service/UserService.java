package com.planup.planup.domain.user.service;

import org.springframework.web.multipart.MultipartFile;
import com.planup.planup.domain.user.entity.User;

public interface UserService {
    User getUserbyUserId(Long userId);

    String getNickname(Long userId);

    String updateNickname(Long userId, String nickname);

    boolean updateNotificationAgree(Long userId);

    boolean checkPassword(Long userId, String password);

    void updatePassword(Long userId, String password);

    String updateProfileImage(Long userId, MultipartFile imageFile);
}
