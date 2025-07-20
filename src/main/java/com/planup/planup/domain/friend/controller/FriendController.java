package com.planup.planup.domain.friend.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.friend.dto.FriendResponseDTO;
import com.planup.planup.domain.friend.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.List;

@Controller
@AllArgsConstructor
@Slf4j
@RequestMapping("/friends")
public class FriendController {

    private final FriendService friendService;

  /*   @GetMapping()
    public ApiResponse<FriendResponseDTO.FriendSummaryList> getFriendList(Long userId) {
        friendService.getFriendSummeryList(userId);
        return null;
    } */
    @Operation(summary = "친구 화면", description = "친구 화면에 진입했을 때 필요한 정보 호출")
    @GetMapping("")
    public ApiResponse<List<FriendResponseDTO.FriendSummaryList>> updateNicknameReq(Long userId) {
        List<FriendResponseDTO.FriendSummaryList> friendSummaryList = friendService.getFriendSummeryList(userId);
        return ApiResponse.onSuccess(friendSummaryList);
    }

    @Operation(summary = "친구 삭제", description = "친구 삭제")
    @PostMapping("/delete")
    public ApiResponse<Boolean> deleteFriend(
            @RequestParam Long userId,
            @RequestParam Long friendId) {
        boolean result = friendService.deleteFriend(userId, friendId);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "친구 차단", description = "친구 차단")
    @PostMapping("/block")
    public ApiResponse<Boolean> blockFriend(
            @RequestParam Long userId,
            @RequestParam Long friendId) {
        boolean result = friendService.blockFriend(userId, friendId);
        return ApiResponse.onSuccess(result);
    }
}
