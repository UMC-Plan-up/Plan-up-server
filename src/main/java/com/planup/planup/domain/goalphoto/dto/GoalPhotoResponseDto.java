package com.planup.planup.domain.goalphoto.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class GoalPhotoResponseDto {

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "개별 사진 정보")
    public static class GoalPhotoDto {
        @Schema(description = "사진 ID", example = "1")
        private Long id;

        @Schema(description = "사진 URL")
        private String photoUrl;

        @Schema(description = "생성 일시")
        private LocalDateTime createdAt;
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "특정 날짜의 사진 목록")
    public static class GoalPhotoListDto {
        @Schema(description = "날짜")
        private LocalDate date;

        @Schema(description = "사진 목록")
        private List<GoalPhotoDto> photos;
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "사진 업로드 결과")
    public static class UploadResultDto {
        @Schema(description = "날짜")
        private LocalDate date;

        @Schema(description = "업로드된 사진 목록")
        private List<GoalPhotoDto> uploadedPhotos;
    }
}
