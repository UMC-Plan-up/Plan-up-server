package com.planup.planup.apiPayload.exception.custom;

import com.planup.planup.apiPayload.code.BaseErrorCode;
import com.planup.planup.apiPayload.exception.GeneralException;
import com.planup.planup.domain.user.dto.UserResponseDTO;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserSuspendedException extends GeneralException {

    private final UserResponseDTO.SanctionInfo sanctionInfo;

    public UserSuspendedException(BaseErrorCode code, LocalDateTime sanctionEndAt, String sanctionReason) {
        super(code);
        this.sanctionInfo = UserResponseDTO.SanctionInfo.builder()
                .sanctionStatus("SUSPENDED")
                .sanctionEndAt(sanctionEndAt)
                .sanctionReason(sanctionReason)
                .build();
    }
}
