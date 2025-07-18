package com.planup.planup.domain.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

public class WeeklyRepoortResponseDTO {

    @Builder
    public record achievementResponse(
            @Schema(description = "뱃지 기록")
            List<bedgeDTO> bedgeDTOList,

            @Schema(description = "친구 알림")
            List<AlarmDTO> alarmDTOList,

            @Schema(description = "응원 메시지")
            String cheering
            ) {
    }

    @Builder
    public record bedgeDTO(
            @Schema(description = "뱃지 id", example = "5")
            Long bedgeId,

            @Schema(description = "뱃지 이름", example = "영향력 있는 시작")
            String bedgeName
    ) {

    }

    @Builder
    public record AlarmDTO(
            @Schema(description = "알림 아이디", example = "1")
            Long id,

            @Schema(description = "알림 내용", example = "친구 1님의 댓글 '벌써 이만큼 함?'")
            String alarmText
    ) {}

}
