package com.planup.planup.validation.jwt;


import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.enums.UserActivate;
import com.planup.planup.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndUserActivate(email, UserActivate.ACTIVE)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        return createUserDetails(user);
    }

    private UserDetails createUserDetails(User user) {
        String password = user.getPassword();
        if (password == null) {
            // Spring Security는 비밀번호가 null이면 인증 실패로 간주할 수 있으므로 임시 값 설정
            password = ""; 
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(password)
                .authorities(user.getRole().name()) // 실제 사용자의 Role 사용
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}