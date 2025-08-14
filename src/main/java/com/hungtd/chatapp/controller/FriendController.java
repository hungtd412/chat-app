package com.hungtd.chatapp.controller;

import com.hungtd.chatapp.dto.request.FriendRequestRequest;
import com.hungtd.chatapp.dto.response.ApiResponse;
import com.hungtd.chatapp.dto.response.FriendRequestResponse;
import com.hungtd.chatapp.dto.response.UserNameAndAvatarResponse;
import com.hungtd.chatapp.service.friend.FriendService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FriendController {

    FriendService friendService;

    @GetMapping("/friends")
    public ResponseEntity<ApiResponse<Page<UserNameAndAvatarResponse>>> getAllFriends(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<UserNameAndAvatarResponse> friends = friendService.getAllFriends(page, size);

        return ResponseEntity.ok(
                ApiResponse.<Page<UserNameAndAvatarResponse>>builder()
                        .data(friends)
                        .build()
        );
    }
    
    @RequestMapping("/friend-requests")
    public static class FriendRequestController {
        
        private final FriendService friendService;
        
        public FriendRequestController(FriendService friendService) {
            this.friendService = friendService;
        }
        
        @PostMapping
        public ResponseEntity<ApiResponse<FriendRequestResponse>> sendFriendRequest(
                @RequestBody @Valid FriendRequestRequest friendRequestRequest) {
            FriendRequestResponse response = friendService.sendFriendRequest(friendRequestRequest);
            
            return ResponseEntity.ok(
                    ApiResponse.<FriendRequestResponse>builder()
                            .data(response)
                            .build()
            );
        }

        @PutMapping("/{requestId}/accept")
        public ResponseEntity<ApiResponse<FriendRequestResponse>> acceptFriendRequest(
                @PathVariable Long requestId) {
            FriendRequestResponse response = friendService.acceptFriendRequest(requestId);
            
            return ResponseEntity.ok(
                    ApiResponse.<FriendRequestResponse>builder()
                            .data(response)
                            .build()
            );
        }

        @DeleteMapping("/{requestId}/reject")
        public ResponseEntity<ApiResponse<Void>> rejectFriendRequest(
                @PathVariable Long requestId) {
            friendService.rejectFriendRequest(requestId);
            
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .build()
            );
        }

        @GetMapping("/pending")
        public ResponseEntity<ApiResponse<List<FriendRequestResponse>>> getPendingRequests() {
            List<FriendRequestResponse> pendingRequests = friendService.getPendingRequests();
            
            return ResponseEntity.ok(
                    ApiResponse.<List<FriendRequestResponse>>builder()
                            .data(pendingRequests)
                            .build()
            );
        }

        @GetMapping("/sent")
        public ResponseEntity<ApiResponse<List<FriendRequestResponse>>> getSentRequests() {
            List<FriendRequestResponse> sentRequests = friendService.getSentRequests();
            
            return ResponseEntity.ok(
                    ApiResponse.<List<FriendRequestResponse>>builder()
                            .data(sentRequests)
                            .build()
            );
        }
    }
}