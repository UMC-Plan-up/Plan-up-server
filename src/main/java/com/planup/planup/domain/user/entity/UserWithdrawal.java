package com.planup.planup.domain.user.entity;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserWithdrawal extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String reason;

    @Column(length = 1000)
    private String detailReason; // 추가 상세 이유 (선택사항)

    @Column(nullable = false)
    private String email; // 탈퇴 시점의 이메일 저장

    @Column(nullable = false)
    private String nickname; // 탈퇴 시점의 닉네임 저장
}