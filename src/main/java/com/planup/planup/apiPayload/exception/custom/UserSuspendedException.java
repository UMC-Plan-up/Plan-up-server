package com.planup.planup.apiPayload.exception.custom;

import com.planup.planup.apiPayload.code.BaseErrorCode;
import com.planup.planup.apiPayload.exception.GeneralException;
import com.planup.planup.domain.user.dto.UserResponseDTO;
import com.planup.planup.domain.user.enums.SanctionDetailReason;
import com.planup.planup.domain.user.enums.SanctionReason;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserSuspendedException extends GeneralException {

    private final UserResponseDTO.SanctionInfo sanctionInfo;

    public UserSuspendedException(BaseErrorCode code, String sanctionStatus, LocalDateTime sanctionEndAt, SanctionReason sanctionReason, SanctionDetailReason sanctionDetailReason, int reportCount) {
        super(code);
        this.sanctionInfo = UserResponseDTO.SanctionInfo.builder()
                .sanctionStatus(sanctionStatus)
                .sanctionEndAt(sanctionEndAt)
                .sanctionReason(sanctionReason)
                .sanctionDetailReason(sanctionDetailReason)
                .reportCount(reportCount)
                .build();
    }
}
