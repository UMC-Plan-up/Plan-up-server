package com.planup.planup.domain.goalphoto.service;

import com.planup.planup.domain.global.service.ImageUploadService;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goal.service.UserGoalService;
import com.planup.planup.domain.goalphoto.dto.GoalPhotoResponseDto;
import com.planup.planup.domain.goalphoto.entity.GoalPhoto;
import com.planup.planup.domain.goalphoto.repository.GoalPhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoalPhotoService {

    private final GoalPhotoRepository goalPhotoRepository;
    private final UserGoalService userGoalService;
    private final ImageUploadService imageUploadService;

    @Transactional
    public GoalPhotoResponseDto.UploadResultDto uploadGoalPhotos(Long userId, Long goalId, LocalDate date, List<MultipartFile> files) {
        UserGoal userGoal = userGoalService.getByGoalIdAndUserId(goalId, userId);

        List<GoalPhotoResponseDto.GoalPhotoDto> uploadedPhotos = new ArrayList<>();

        for (MultipartFile file : files) {
            String photoUrl = imageUploadService.uploadImage(file, "goals/photos");

            GoalPhoto goalPhoto = GoalPhoto.builder()
                    .photoUrl(photoUrl)
                    .date(date)
                    .userGoal(userGoal)
                    .build();

            GoalPhoto saved = goalPhotoRepository.save(goalPhoto);

            uploadedPhotos.add(GoalPhotoResponseDto.GoalPhotoDto.builder()
                    .id(saved.getId())
                    .photoUrl(saved.getPhotoUrl())
                    .createdAt(saved.getCreatedAt())
                    .build());
        }

        return GoalPhotoResponseDto.UploadResultDto.builder()
                .date(date)
                .uploadedPhotos(uploadedPhotos)
                .build();
    }

    public GoalPhotoResponseDto.GoalPhotoListDto getGoalPhotosByDate(Long userId, Long goalId, LocalDate date) {
        UserGoal userGoal = userGoalService.getByGoalIdAndUserId(goalId, userId);

        List<GoalPhoto> photos = goalPhotoRepository.findAllByUserGoalAndDateOrderByCreatedAtDesc(userGoal, date);

        List<GoalPhotoResponseDto.GoalPhotoDto> photoDtos = photos.stream()
                .map(photo -> GoalPhotoResponseDto.GoalPhotoDto.builder()
                        .id(photo.getId())
                        .photoUrl(photo.getPhotoUrl())
                        .createdAt(photo.getCreatedAt())
                        .build())
                .toList();

        return GoalPhotoResponseDto.GoalPhotoListDto.builder()
                .date(date)
                .photos(photoDtos)
                .build();
    }

    @Transactional
    public void deleteGoalPhoto(Long userId, Long photoId) {
        GoalPhoto goalPhoto = goalPhotoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("사진을 찾을 수 없습니다."));

        UserGoal userGoal = goalPhoto.getUserGoal();
        if (!userGoal.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 사진에 대한 권한이 없습니다.");
        }

        imageUploadService.deleteImage(goalPhoto.getPhotoUrl());
        goalPhotoRepository.delete(goalPhoto);
    }
}
