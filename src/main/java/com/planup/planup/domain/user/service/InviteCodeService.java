package com.planup.planup.domain.user.service;

import com.planup.planup.domain.user.dto.AuthResponseDTO;

public interface InviteCodeService {

    AuthResponseDTO.InviteCode getMyInviteCode(Long userId);

    Long findInviterByCode(String inviteCode);
}
