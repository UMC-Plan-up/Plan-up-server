package com.planup.planup.domain.bedge.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Builder @AllArgsConstructor @NoArgsConstructor
public class SpecificGoalDays {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long goalId;
    private Long userId;
    private LocalDate lastUpdate;
    @Builder.Default
    private int consecutiveSuccessDays = 0;

}
