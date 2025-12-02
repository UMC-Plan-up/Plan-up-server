package com.planup.planup.domain.user.service.command;

import com.planup.planup.domain.user.dto.AuthResponseDTO;
import com.planup.planup.domain.user.dto.FileResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface UserProfileCommandService {
    // 기본 정보 수정
    String updateNickname(Long userId, String nickname);
    boolean updateMarketingNotificationAllow(Long userId);
    boolean updateServiceNotificationAllow(Long userId);

    // 프로필 이미지
    FileResponseDTO.ImageUpload uploadProfileImage(MultipartFile file, String email);
    FileResponseDTO.ImageUpload updateProfileImage(Long userId, MultipartFile file);

    // 이메일 변경
    String updateEmail(Long userId, String newEmail);
    AuthResponseDTO.EmailSend sendEmailChangeVerification(Long userId, String newEmail);
    AuthResponseDTO.EmailSend sendEmailChangeVerification(String currentEmail, String newEmail);
    AuthResponseDTO.EmailSend resendEmailChangeVerification(Long userId, String newEmail);
    AuthResponseDTO.EmailSend resendEmailChangeVerification(String currentEmail, String newEmail);
    void completeEmailChange(String token);
    String handleEmailChangeLink(String token);

    // 토큰 관리
    String completeVerification(String verificationToken);
    String validateToken(String token);
    String[] validatePasswordChangeToken(String token);
    String validateEmailChangeToken(String token);
    void clearVerificationToken(String email);
    void clearPasswordChangeToken(String email);
    void clearEmailChangeToken(String token);
}
