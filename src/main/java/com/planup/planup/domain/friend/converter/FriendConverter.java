package com.planup.planup.domain.friend.converter;

import com.planup.planup.domain.friend.dto.FriendResponseDTO;
import com.planup.planup.domain.user.entity.User;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class FriendConverter {

    public static FriendResponseDTO.FriendInfoSummary toFriendSummery(User friend) {
        return FriendResponseDTO.FriendInfoSummary
                .builder()
                .id(friend.getId())
                .goalCnt()
                .isNewPhotoVerify()
                .build();

    }
}
