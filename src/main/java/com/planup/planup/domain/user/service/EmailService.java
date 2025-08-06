package com.planup.planup.domain.user.service;

public interface EmailService {

    void sendVerificationLink(String email);

    String verifyToken(String token);

    void resendVerificationLink(String email);

    boolean isEmailVerified(String email);

    void clearVerificationToken(String email);
}