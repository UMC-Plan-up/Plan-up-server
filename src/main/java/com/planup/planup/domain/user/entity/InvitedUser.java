package com.planup.planup.domain.user.entity;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class InvitedUser extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "invitedUser_id")
    private User invitedUser;
}
