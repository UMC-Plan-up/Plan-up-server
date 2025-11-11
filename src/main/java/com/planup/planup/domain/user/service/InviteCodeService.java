package com.planup.planup.domain.user.service;

import com.planup.planup.domain.user.dto.InviteCodeResponseDTO;

public interface InviteCodeService {

    InviteCodeResponseDTO getMyInviteCode(Long userId);

    Long findInviterByCode(String inviteCode);
}
