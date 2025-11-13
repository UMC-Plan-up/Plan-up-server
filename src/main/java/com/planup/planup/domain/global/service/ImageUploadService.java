package com.planup.planup.domain.global.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageUploadService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    public String uploadImage(MultipartFile file, String folderPath) {
        validateImageFile(file);

        String fileName = generateFileName(file.getOriginalFilename());
        String fullPath = folderPath + "/" + fileName;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fullPath)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return String.format("https://%s.s3.%s.amazonaws.com/%s",
                    bucketName, region, fullPath);

        } catch (IOException e) {
            log.error("이미지 업로드 실패: {}", e.getMessage());
            throw new RuntimeException("이미지 업로드에 실패했습니다.");
        }
    }

    private void validateImageFile(MultipartFile file) {

        if (file == null) {
            throw new IllegalArgumentException("파일이 전달되지 않았습니다.");
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 5MB를 초과할 수 없습니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
    }

    /**
     * S3에서 이미지 삭제
     * @param imageUrl 삭제할 이미지의 전체 URL
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return;
        }

        try {
            // S3 URL에서 키 추출
            String key = extractKeyFromUrl(imageUrl);
            if (key == null) {
                log.warn("유효하지 않은 S3 URL: {}", imageUrl);
                return;
            }

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("S3 이미지 삭제 완료: {}", key);

        } catch (Exception e) {
            log.error("S3 이미지 삭제 실패: {}, URL: {}", e.getMessage(), imageUrl);
            // 삭제 실패해도 예외를 던지지 않음 (기존 이미지가 없을 수도 있음)
        }
    }

    /**
     * S3 URL에서 키 추출
     * @param imageUrl S3 이미지 URL
     * @return S3 키
     */
    private String extractKeyFromUrl(String imageUrl) {
        try {
            // URL 형식: https://bucket-name.s3.region.amazonaws.com/key
            String baseUrl = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, region);
            if (imageUrl.startsWith(baseUrl)) {
                return imageUrl.substring(baseUrl.length());
            }
            return null;
        } catch (Exception e) {
            log.error("URL에서 키 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}