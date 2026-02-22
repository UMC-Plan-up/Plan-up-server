package com.planup.planup.domain.goalphoto.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.goalphoto.dto.GoalPhotoResponseDto;
import com.planup.planup.domain.goalphoto.service.GoalPhotoService;
import com.planup.planup.validation.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/goals")
public class GoalPhotoController {

    private final GoalPhotoService goalPhotoService;

    @PostMapping("/{goalId}/photos")
    @Operation(summary = "목표 사진 업로드", description = "특정 날짜에 목표 사진을 업로드합니다. 여러 장 가능.")
    public ApiResponse<GoalPhotoResponseDto.UploadResultDto> uploadGoalPhotos(
            @PathVariable Long goalId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestPart List<MultipartFile> files,
            @CurrentUser Long userId) {

        GoalPhotoResponseDto.UploadResultDto result = goalPhotoService.uploadGoalPhotos(userId, goalId, date, files);
        return ApiResponse.onSuccess(result);
    }

    @GetMapping("/{goalId}/photos")
    @Operation(summary = "목표 사진 조회", description = "특정 날짜의 목표 사진 목록을 조회합니다.")
    public ApiResponse<GoalPhotoResponseDto.GoalPhotoListDto> getGoalPhotos(
            @PathVariable Long goalId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @CurrentUser Long userId) {

        GoalPhotoResponseDto.GoalPhotoListDto result = goalPhotoService.getGoalPhotosByDate(userId, goalId, date);
        return ApiResponse.onSuccess(result);
    }

    @DeleteMapping("/photos/{photoId}")
    @Operation(summary = "목표 사진 삭제", description = "개별 목표 사진을 삭제합니다.")
    public ApiResponse<Void> deleteGoalPhoto(
            @Parameter(description = "사진 ID", example = "1")
            @PathVariable Long photoId,
            @CurrentUser Long userId) {

        goalPhotoService.deleteGoalPhoto(userId, photoId);
        return ApiResponse.onSuccess(null);
    }
}
