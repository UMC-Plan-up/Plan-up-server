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

}
