package com.planup.planup.domain.oauth.entity;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import com.planup.planup.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OAuthAccount extends BaseTimeEntity {

    @Id @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private AuthProvideerEnum provider; // GOOGLE, APPLE, KAKAO 등

    private String email; // 해당 플랫폼에서 발급된 유저 식별자

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public void setMember(User member) {
        this.user = member;
        member.getOAuthAccountList().add(this);
    }
}
