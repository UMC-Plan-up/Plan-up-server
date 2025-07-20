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
import org.springframework.web.bind.annotation.RequestBody;
import com.planup.planup.domain.friend.dto.FriendReportRequestDTO;


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
    @Operation(summary = "친구 화면 조회", description = "친구 화면에 진입했을 때 필요한 정보 조회")
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

    @Operation(summary = "친구 신고", description = "친구 신고 및 차단")
    @PostMapping("/report")
    public ApiResponse<Boolean> reportFriend(@RequestBody FriendReportRequestDTO request) {
        boolean result = friendService.reportFriend(
            request.getUserId(),
            request.getFriendId(),
            request.getReason(),
            request.isBlock()
        );
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "나에게 친구 신청한 친구 목록", description = "나에게 친구 신청한 친구 목록 조회")
    @GetMapping("/requests")
    public ApiResponse<List<FriendResponseDTO.FriendInfoSummary>> getRequestedFriends(@RequestParam Long userId) {
        List<FriendResponseDTO.FriendInfoSummary> requestedFriends = friendService.getRequestedFriends(userId);
        return ApiResponse.onSuccess(requestedFriends);
    }
}
