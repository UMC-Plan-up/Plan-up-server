package com.planup.planup.domain.user.service;

import com.planup.planup.domain.user.dto.*;
import org.springframework.web.multipart.MultipartFile;
import com.planup.planup.domain.user.entity.User;

public interface UserService {
    User getUserbyUserId(Long userId);

    String getNickname(Long userId);

    String updateNickname(Long userId, String nickname);

    boolean updateNotificationAgree(Long userId);

    boolean checkPassword(Long userId, String password);

    void updatePassword(Long userId, String password);



    UserInfoResponseDTO getUserInfo(Long userId);

    String updateEmail(Long userId, String newEmail);
    
    SignupResponseDTO signup(SignupRequestDTO request);

    LoginResponseDTO login(LoginRequestDTO request);

    KakaoAccountResponseDTO getKakaoAccountStatus(Long userId);

    ImageUploadResponseDTO uploadProfileImage(MultipartFile file, User currentUser);

    InviteCodeResponseDTO getMyInviteCode(Long userId);

    ValidateInviteCodeResponseDTO validateInviteCode(String inviteCode, Long currentUserId);
}