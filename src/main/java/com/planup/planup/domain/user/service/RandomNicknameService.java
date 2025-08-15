package com.planup.planup.domain.user.service;

import com.planup.planup.domain.user.dto.RandomNicknameResponseDTO;

public interface RandomNicknameService {
    // 랜덤 닉네임 생성
    RandomNicknameResponseDTO generateRandomNickname();
}
