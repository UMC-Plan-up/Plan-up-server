package com.planup.planup.domain.friend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

public class FriendResponseDTO {

    @Builder
    public record FriendSummaryList(
            @Schema(description = "친구 수", example = "5")
            int cnt,

            @Schema(description = "친구 간단 정보")
            List<FriendInfoSummary> friendInfoSummaryList
            ) {}


    @Builder
    @Schema(description = "친구 요약 정보 DTO")
    public record FriendInfoSummary(

            @Schema(description = "친구 기본키", example = "1")
            Long id,

            @Schema(description = "친구 닉네임", example = "홍길동")
            String nickname, 

            @Schema(description = "목표 수행 수", example = "3")
            int goalCnt,

            @Schema(description = "오늘 진행한 시간", example = "00:01:54")
            LocalDate todayTime,

            @Schema(description = "새로운 사진 인증 여부", example = "true")
            boolean isNewPhotoVerify
    ) {}
}
