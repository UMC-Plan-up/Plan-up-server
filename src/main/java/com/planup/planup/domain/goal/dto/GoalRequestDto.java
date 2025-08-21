package com.planup.planup.domain.goal.dto;

import com.planup.planup.domain.goal.entity.Enum.GoalCategory;
import com.planup.planup.domain.goal.entity.Enum.GoalPeriod;
import com.planup.planup.domain.goal.entity.Enum.GoalType;
import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

public class GoalRequestDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateGoalDto {
        String goalName;
        String goalAmount;
        GoalCategory goalCategory;
        GoalType goalType;
        int oneDose;
        int frequency;
        GoalPeriod period;
        LocalDate endDate;
        VerificationType verificationType;
        int limitFriendCount;
        Integer goalTime;
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "메모 작성/수정 요청 DTO")
    public static class CreateMemoRequestDto {
        @Size(max = 50, message = "메모는 50자 이내로 작성해주세요")
        @Schema(description = "메모 내용", example = "오늘의 메모", maxLength = 50)
        private String memo;
        private LocalDate memoDate;

        public boolean isEmpty() {
            return memo == null || memo.trim().isEmpty();
        }

        public boolean hasContent() {
            return memo != null && !memo.trim().isEmpty();
        }

        public String getTrimmedMemo() {
            return memo != null ? memo.trim() : "";
        }
    }
}
