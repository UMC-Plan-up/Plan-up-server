package com.planup.planup.domain.user.service;

<<<<<<< HEAD
import com.planup.planup.domain.user.dto.UserInfoResponseDTO;
import org.springframework.web.multipart.MultipartFile;
=======
import com.planup.planup.domain.user.dto.LoginRequestDTO;
import com.planup.planup.domain.user.dto.LoginResponseDTO;
import com.planup.planup.domain.user.dto.SignupRequestDTO;
import com.planup.planup.domain.user.dto.SignupResponseDTO;
>>>>>>> 4c1817c1fd3a4d5ba7f3a3742725f5d6d30b0e8f
import com.planup.planup.domain.user.entity.User;

public interface UserService {
    User getUserbyUserId(Long userId);

    String getNickname(Long userId);

    String updateNickname(Long userId, String nickname);

    boolean updateNotificationAgree(Long userId);

    boolean checkPassword(Long userId, String password);

    void updatePassword(Long userId, String password);

<<<<<<< HEAD
    String updateProfileImage(Long userId, MultipartFile imageFile);

    UserInfoResponseDTO getUserInfo(Long userId);

    String updateEmail(Long userId, String newEmail);
=======
    SignupResponseDTO signup(SignupRequestDTO request);

    LoginResponseDTO login(LoginRequestDTO request);
>>>>>>> 4c1817c1fd3a4d5ba7f3a3742725f5d6d30b0e8f
}
