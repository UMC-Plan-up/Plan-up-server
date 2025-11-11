package com.planup.planup.domain.user.service;

import com.planup.planup.domain.user.dto.AuthResponseDTO;

public interface EmailService {

    // 이메일 인증 링크 발송 및 토큰 반화
    String sendVerificationEmail(String email);

    // 이메일 인증 메일 재발송
    String resendVerificationEmail(String email);

    // 토큰 유효성 확인
    String validateToken(String token);

    // 토큰으로 이메일 인증 완료
    String completeVerification(String verificationToken);

    // 이메일 인증 확인
    boolean isEmailVerified(String email);

    // 이메일 인증 토큰 정리
    void clearVerificationToken(String email);

    // 비밀번호 변경용 딥링크 이메일 발송
    String sendPasswordChangeEmail(String email, Boolean isLoggedIn);

    // 비밀번호 변경 이메일 재발송
    String resendPasswordChangeEmail(String email, Boolean isLoggedIn);

    
    // 비밀번호 변경 이메일 인증 완료 여부 확인
    Boolean isPasswordChangeEmailVerified(String email);
    
    // 비밀번호 변경 이메일 인증 완료 표시
    void markPasswordChangeEmailAsVerified(String email);

    // 비밀번호 변경 토큰에서 이메일과 로그인 상태 정보 모두 반환
    String[] validatePasswordChangeToken(String token);


    String createSuccessHtml(String email, String deepLinkUrl);

    String createFailureHtml();



    // 이메일 변경용 인증 메일 발송
    String sendEmailChangeVerificationEmail(String currentEmail, String newEmail);

    // 이메일 변경 토큰 검증
    String validateEmailChangeToken(String token);

    // 이메일 변경 링크 처리 및 응답 DTO 생성
    AuthResponseDTO.EmailVerifyLink handleEmailChangeLink(String token);

    // 이메일 변경 인증 메일 재발송
    String resendEmailChangeVerificationEmail(String currentEmail, String newEmail);
    
    // 이메일 변경 토큰 정리
    void clearEmailChangeToken(String token);

    // 비밀번호 변경 토큰 정리
    void clearPasswordChangeToken(String email);
}