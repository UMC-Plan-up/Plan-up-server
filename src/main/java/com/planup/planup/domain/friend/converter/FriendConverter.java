package com.planup.planup.domain.friend.converter;

import com.planup.planup.domain.friend.dto.FriendResponseDTO;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.verification.service.TimerVerificationService;
import com.planup.planup.domain.verification.repository.PhotoVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FriendConverter {


    public FriendResponseDTO.FriendInfoSummary toFriendSummary(
            User friend,
            int goalCnt,
            boolean isNewPhotoVerify,
            LocalTime todayTime
    ) {
        return FriendResponseDTO.FriendInfoSummary.builder()
                .id(friend.getId())
                .nickname(friend.getNickname())
                .goalCnt(goalCnt)
                .todayTime(todayTime)
                .isNewPhotoVerify(isNewPhotoVerify)
                .build();
    }

    public FriendResponseDTO.FriendInfoInChallengeCreate toFriendInfoChallenge(User friend) {
        return FriendResponseDTO.FriendInfoInChallengeCreate
                .builder()
                .id(friend.getId())
                .nickname(friend.getNickname())
                .goalCnt(friend.getUserGoals().size())
                .build();
    }

    public FriendResponseDTO.FriendSummaryList toFriendSummaryList(List<FriendResponseDTO.FriendInfoSummary> items) {
        return FriendResponseDTO.FriendSummaryList.builder()
                .cnt(items.size())
                .friendInfoSummaryList(items)
                .build();
    }
    
    /**
     * 사용자의 모든 목표에 대한 오늘 타이머 시간을 합계하여 계산
     */



}
