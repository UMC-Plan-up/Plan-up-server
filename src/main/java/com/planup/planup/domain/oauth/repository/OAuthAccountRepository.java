package com.planup.planup.domain.oauth.repository;

import com.planup.planup.domain.oauth.entity.AuthProvideerEnum;
import com.planup.planup.domain.oauth.entity.OAuthAccount;
import com.planup.planup.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {
    
    // 특정 사용자의 특정 플랫폼 OAuth 계정 조회
    Optional<OAuthAccount> findByUserAndProvider(User user, AuthProvideerEnum provider);
    
    // 특정 사용자의 특정 플랫폼 OAuth 계정 존재 여부 확인
    boolean existsByUserAndProvider(User user, AuthProvideerEnum provider);

    Optional<OAuthAccount> findByEmailAndProvider(String email, AuthProvideerEnum provider);
}