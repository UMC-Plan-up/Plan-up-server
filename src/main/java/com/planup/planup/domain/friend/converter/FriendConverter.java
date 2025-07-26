package com.planup.planup.domain.friend.converter;

import com.planup.planup.domain.friend.dto.FriendResponseDTO;
import com.planup.planup.domain.user.entity.User;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class FriendConverter {

    public static FriendResponseDTO.FriendInfoSummary toFriendSummary(User friend) {
        return FriendResponseDTO.FriendInfoSummary
                .builder()
                .id(friend.getId())
                .nickname(friend.getNickname()) 
                .goalCnt(0)
                .isNewPhotoVerify(true)
                .build();

    }

    public static FriendResponseDTO.FriendInfoInChallengeCreate toFriendInfoChallenge(User friend) {
        return FriendResponseDTO.FriendInfoInChallengeCreate
                .builder()
                .id(friend.getId())
                .nickname(friend.getNickname())
                //TODO: GoalCount 조회 메서드 추가
                .goalCnt(0)
                .build();

    }

        public static FriendResponseDTO.FriendSummaryList toFriendSummaryList(List<User> friends) {
        List<FriendResponseDTO.FriendInfoSummary> summeryList = friends.stream().map(FriendConverter::toFriendSummary).collect(Collectors.toList());
        int size = summeryList.size();

        return FriendResponseDTO.FriendSummaryList
                .builder()
                .cnt(size)
                .friendInfoSummaryList(summeryList)
                .build();
    }
}
