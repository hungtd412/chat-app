package com.hungtd.chatapp.controller;

import com.hungtd.chatapp.dto.request.FriendRequestRequest;
import com.hungtd.chatapp.dto.response.ApiResponse;
import com.hungtd.chatapp.dto.response.FriendRequestResponse;
import com.hungtd.chatapp.service.friend.FriendRequestService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friend-requests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FriendController {

    FriendRequestService friendRequestService;

    @PostMapping
    public ResponseEntity<ApiResponse<FriendRequestResponse>> sendFriendRequest(
            @RequestBody @Valid FriendRequestRequest friendRequestRequest) {
        FriendRequestResponse response = friendRequestService.sendFriendRequest(friendRequestRequest);
        
        return ResponseEntity.ok(
                ApiResponse.<FriendRequestResponse>builder()
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{requestId}/accept")
    public ResponseEntity<ApiResponse<FriendRequestResponse>> acceptFriendRequest(
            @PathVariable Long requestId) {
        FriendRequestResponse response = friendRequestService.acceptFriendRequest(requestId);
        
        return ResponseEntity.ok(
                ApiResponse.<FriendRequestResponse>builder()
                        .data(response)
                        .build()
        );
    }

    @DeleteMapping("/{requestId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectFriendRequest(
            @PathVariable Long requestId) {
        friendRequestService.rejectFriendRequest(requestId);
        
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .build()
        );
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<FriendRequestResponse>>> getPendingRequests() {
        List<FriendRequestResponse> pendingRequests = friendRequestService.getPendingRequests();
        
        return ResponseEntity.ok(
                ApiResponse.<List<FriendRequestResponse>>builder()
                        .data(pendingRequests)
                        .build()
        );
    }

    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<List<FriendRequestResponse>>> getSentRequests() {
        List<FriendRequestResponse> sentRequests = friendRequestService.getSentRequests();
        
        return ResponseEntity.ok(
                ApiResponse.<List<FriendRequestResponse>>builder()
                        .data(sentRequests)
                        .build()
        );
    }
}
