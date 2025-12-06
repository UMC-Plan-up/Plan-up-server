package com.planup.planup.domain.user.entity;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;


@Entity
@Table(name = "user_terms")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserTerms extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저와 약관은 연관관계로 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id")
    private Terms terms;

    private Boolean isAgreed;
    private LocalDateTime agreedAt;

    public Boolean setIsAgreed(Boolean b) {
        this.isAgreed = b;
        if (b) this.agreedAt = LocalDateTime.now();
        return this.isAgreed;
    }
}
