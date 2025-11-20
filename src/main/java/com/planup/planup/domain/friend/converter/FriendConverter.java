package com.planup.planup.domain.friend.converter;

import com.planup.planup.domain.friend.dto.BlockedFriendResponseDTO;
import com.planup.planup.domain.friend.dto.FriendResponseDTO;
import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.goal.dto.UserWithGoalCountDTO;
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
            LocalTime todayTime,
            String profile
    ) {
        return FriendResponseDTO.FriendInfoSummary.builder()
                .id(friend.getId())
                .nickname(friend.getNickname())
                .goalCnt(goalCnt)
                .todayTime(todayTime)
                .isNewPhotoVerify(isNewPhotoVerify)
                .profileImg(profile)
                .build();
    }

    public List<FriendResponseDTO.FriendInfoInChallengeCreate> toFriendInfoChallenge(List<UserWithGoalCountDTO> dtos) {
        return dtos.stream().map(dto -> {
            FriendResponseDTO.FriendInfoInChallengeCreate create = FriendResponseDTO.FriendInfoInChallengeCreate
                    .builder()
                    .id(dto.getUser().getId())
                    .nickname(dto.getUser().getNickname())
                    .goalCnt(dto.getGoalCnt())
                    .build();
            return create;
        }).collect(Collectors.toList());
    }

    public static FriendResponseDTO.FriendSummaryList toFriendSummaryList(List<FriendResponseDTO.FriendInfoSummary> items) {
        return FriendResponseDTO.FriendSummaryList.builder()
                .cnt(items.size())
                .friendInfoSummaryList(items)
                .build();
    }
    
    /**
     * 사용자의 모든 목표에 대한 오늘 타이머 시간을 합계하여 계산
     */


    public List<BlockedFriendResponseDTO> toBlockedFriendDTO(Long userId, List<User> friends) {
        return friends.stream()
                .map(friend -> {
                    return BlockedFriendResponseDTO.builder()
                            .friendId(friend.getId())
                            .friendNickname(friend.getNickname())
                            .profileImg(friend.getProfileImg())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
