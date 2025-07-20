package com.planup.planup.domain.friend.converter;

import com.planup.planup.domain.friend.dto.FriendResponseDTO;
import com.planup.planup.domain.user.entity.User;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class FriendConverter {

    public static FriendResponseDTO.FriendInfoSummary toFriendSummery(User friend) {
        return FriendResponseDTO.FriendInfoSummary
                .builder()
                .id(friend.getId())
                .goalCnt(0)
                .isNewPhotoVerify(true)
                .build();

    }

    public static FriendResponseDTO.FriendSummaryList toFriendSummeryList(List<User> friends) {
        List<FriendResponseDTO.FriendInfoSummary> summeryList = friends.stream().map(FriendConverter::toFriendSummery).collect(Collectors.toList());
        int size = summeryList.size();

        return FriendResponseDTO.FriendSummaryList
                .builder()
                .cnt(size)
                .friendInfoSummeryList(summeryList)
                .build();
    }
}
