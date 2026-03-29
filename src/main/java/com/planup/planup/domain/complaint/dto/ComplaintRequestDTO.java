package com.planup.planup.domain.complaint.dto;

import com.planup.planup.domain.user.enums.SanctionDetailReason;
import lombok.Getter;

@Getter
public class ComplaintRequestDTO {
    private SanctionDetailReason reason;
}
