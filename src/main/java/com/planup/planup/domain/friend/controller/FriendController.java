package com.planup.planup.domain.friend.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.friend.dto.FriendResponseDTO;
import com.planup.planup.domain.friend.dto.BlockedFriendResponseDTO;
import com.planup.planup.domain.friend.dto.UnblockFriendRequestDTO;
import com.planup.planup.domain.friend.repository.UserReportMappingRepository;
import com.planup.planup.domain.friend.service.FriendReadService;
import com.planup.planup.domain.friend.service.FriendWriteService;
import com.planup.planup.domain.friend.service.reportUserService.UserReportMappingService;
import com.planup.planup.domain.friend.service.reportUserService.UserReportMappingServiceImpl;
import com.planup.planup.domain.friend.service.userBlockService.UserBlockService;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.validation.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import com.planup.planup.domain.friend.dto.FriendReportRequestDTO;


import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/friends")
public class FriendController {

    private final FriendWriteService friendWriteService;
    private final FriendReadService friendReadService;
    private final UserReportMappingService userReportMappingService;
    private final UserBlockService userBlockService;

    @Operation(summary = "친구 화면 조회", description = "친구 화면에 진입했을 때 필요한 정보 조회")
    @GetMapping("/list")
    public ApiResponse<FriendResponseDTO.FriendSummaryList> getFriendList(@CurrentUser Long userId) {
        FriendResponseDTO.FriendSummaryList friendSummaryList = friendReadService.getFriendSummeryList(userId);
        return ApiResponse.onSuccess(friendSummaryList);
    }

    @Operation(summary = "친구 삭제", description = "친구 삭제")
    @PostMapping("/delete")
    public ApiResponse<Boolean> deleteFriend(
            @CurrentUser User user,
            @RequestParam Long friendId) {
        boolean result = friendWriteService.deleteFriend(user, friendId);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "친구 차단", description = "친구 차단")
    @PostMapping("/block")
    public ApiResponse<Boolean> blockFriend(
            @CurrentUser User user,
            @RequestParam Long friendId) {
        boolean result = userBlockService.blockFriend(user, friendId);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "친구 신고", description = "친구 또는 차단된 친구를 신고하고 필요시 차단합니다")
    @PostMapping("/report")
    public ApiResponse<Boolean> reportFriend(@RequestBody FriendReportRequestDTO request, @CurrentUser Long userId) {
        boolean result = userReportMappingService.createReportUser(request, userId);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "나에게 친구 신청한 친구 목록", description = "나에게 친구 신청한 친구 목록 조회")
    @GetMapping("/requests")
    public ApiResponse<List<FriendResponseDTO.FriendInfoSummary>> getRequestedFriends(@Parameter(hidden = true) @CurrentUser Long userId) {
        List<FriendResponseDTO.FriendInfoSummary> requestedFriends = friendReadService.getRequestedFriends(userId);
        return ApiResponse.onSuccess(requestedFriends);
    }

    @Operation(summary = "친구 신청 거절", description = "친구 신청 거절")
    @PostMapping("/reject")
    public ApiResponse<Boolean> rejectFriendRequest(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @RequestParam Long friendId) {
        boolean result = friendWriteService.rejectFriendRequest(userId, friendId);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "친구 신청 수락", description = "친구 신청 수락")
    @PostMapping("/accept")
    public ApiResponse<Boolean> acceptFriendRequest(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @RequestParam Long friendId) {
        boolean result = friendWriteService.acceptFriendRequest(userId, friendId);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "친구 신청 보내기", description = "타 유저에게 친구 신청을 보냅니다")
    @PostMapping("/request")
    public ApiResponse<Boolean> sendFriendRequest(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @RequestParam Long friendId) {
        boolean result = friendWriteService.sendFriendRequest(userId, friendId);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "차단된 친구 목록 조회", description = "내가 차단한 친구들의 이름 목록을 조회합니다")
    @GetMapping("/blocked")
    public ApiResponse<List<BlockedFriendResponseDTO>> getBlockedFriends(@Parameter(hidden = true) @CurrentUser Long userId) {
        List<BlockedFriendResponseDTO> blockedFriends = userBlockService.getBlockedFriends(userId);
        return ApiResponse.onSuccess(blockedFriends);
    }

    @Operation(summary = "친구 차단 해제", description = "친구 이름으로 차단된 친구를 차단 해제합니다")
    @PostMapping("/unblock")
    public ApiResponse<Boolean> unblockFriend(@RequestBody UnblockFriendRequestDTO request) {
        Long result = userBlockService.unblockFriend(request);
        return ApiResponse.onSuccess(true);
    }
}
