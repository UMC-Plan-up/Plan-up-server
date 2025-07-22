package com.planup.planup.domain.user.entity;

import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.global.entity.BaseTimeEntity;
import com.planup.planup.domain.oauth.entity.OAuthAccount;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String password;
    private String nickname;

    @Enumerated(EnumType.STRING)
    private UserActivate userActivate;

    @Enumerated(EnumType.STRING)
    private UserLevel userLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Lob
    private String profileImg;

    private String socialEmail;
    private Boolean alarmAllow;
    private String inviteCode;

    // 연관 관계
    @OneToMany(mappedBy = "user")
    private List<UserTerms> userTermList;

    @OneToMany(mappedBy = "user")
    private List<InvitedUser> invitedUserList;

    @OneToMany(mappedBy = "user")
    private List<OAuthAccount> oAuthAccountList;

    @OneToMany(mappedBy = "user")
    private List<Friend> friendList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserBadge> userBadges = new ArrayList<>();

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void switchAlarmAllow() {
        if (this.alarmAllow == true) {
            this.alarmAllow = false;
        } else {
            this.alarmAllow = true;
        }
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
