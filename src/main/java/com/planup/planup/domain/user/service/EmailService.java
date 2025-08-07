package com.planup.planup.domain.user.service;

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
}