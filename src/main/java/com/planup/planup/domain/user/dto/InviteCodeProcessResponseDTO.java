package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InviteCodeProcessResponseDTO {

    @Schema(description = "초대코드로 친구가 된 사용자 닉네임", example = "라미")
    private String friendNickname;

    @Schema(description = "처리 결과 메시지", example = "친구 관계가 성공적으로 생성되었습니다.")
    private String message;

    @Schema(description = "처리 성공 여부", example = "true")
    private boolean success;
}
