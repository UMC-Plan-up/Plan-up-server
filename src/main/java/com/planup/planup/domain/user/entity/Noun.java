package com.planup.planup.domain.user.entity;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "noun")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Noun extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String word;


}
