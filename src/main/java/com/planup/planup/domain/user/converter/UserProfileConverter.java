package com.planup.planup.domain.user.converter;

import com.planup.planup.domain.user.dto.AuthResponseDTO;
import com.planup.planup.domain.user.dto.FileResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class UserProfileConverter {

    /**
     * 이미지 업로드 응답 DTO 생성
     */
    public FileResponseDTO.ImageUpload toImageUploadResponseDTO(String imageUrl) {
        return FileResponseDTO.ImageUpload.builder()
                .imageUrl(imageUrl)
                .build();
    }

    /**
     * 이메일 발송 응답 DTO (메시지 커스터마이징 가능)
     */
    public AuthResponseDTO.EmailSend toEmailSendResponseDTO(String email, String verificationToken, String message) {
        return AuthResponseDTO.EmailSend.builder()
                .email(email)
                .message(message)
                .verificationToken(verificationToken)
                .build();
    }
}
