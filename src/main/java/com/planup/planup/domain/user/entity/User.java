package com.planup.planup.domain.user.entity;

import com.planup.planup.domain.bedge.entity.UserStat;
import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.global.entity.BaseTimeEntity;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.oauth.entity.OAuthAccount;
import com.planup.planup.domain.report.entity.WeeklyReport;
import com.planup.planup.domain.user.enums.Role;
import com.planup.planup.domain.user.enums.UserActivate;
import com.planup.planup.domain.user.enums.UserLevel;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@SuperBuilder
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Column(name = "alarm_allow", nullable = false)
    @Builder.Default
    private Boolean marketingNotificationAllow = false; // 혜택 및 마케팅 알림 동의
    
    @Column(name = "service_notification_allow", nullable = false)
    @Builder.Default
    private Boolean serviceNotificationAllow = true; // 서비스 알림 (기본값: true)
    
    private String inviteCode;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    // 연관 관계
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserTerms> userTermList = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<InvitedUser> invitedUserList;

    @OneToMany(mappedBy = "user")
    private List<OAuthAccount> oAuthAccountList;

    @OneToMany(mappedBy = "user")
    private List<Friend> friendList;


    @OneToMany(mappedBy = "user")
    private List<WeeklyReport> weeklyReportList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserBadge> userBadges = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserGoal> userGoals = new ArrayList<>();

    @OneToOne(mappedBy = "user")
    private UserStat userStat;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserWithdrawal userWithdrawal;

    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerifiedAt = LocalDateTime.now();
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateMarketingNotificationAllow() {
        if (this.marketingNotificationAllow == true) {
            this.marketingNotificationAllow = false;
        } else {
            this.marketingNotificationAllow = true;
        }
    }

    public void updateServiceNotificationAllow() {
        if (this.serviceNotificationAllow == true) {
            this.serviceNotificationAllow = false;
        } else {
            this.serviceNotificationAllow = true;
        }
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void addUserGoal(UserGoal userGoal) {
        userGoals.add(userGoal);
        userGoal.setUser(this);
    }

    public void updateProfileImage(String profileImg) {
        this.profileImg = profileImg;
    }
}
