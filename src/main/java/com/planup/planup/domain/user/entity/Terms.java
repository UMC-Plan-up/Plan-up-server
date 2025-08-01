package com.planup.planup.domain.user.entity;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Getter
@Table(name = "terms")
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
public class Terms extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String summary;

    @OneToMany(mappedBy = "terms", cascade = CascadeType.ALL)
    private List<UserTerms> userTerms;

    @Lob
    private String content;

    private Boolean isRequired;

    @Column(name = "`order`")
    private Integer order;
}
