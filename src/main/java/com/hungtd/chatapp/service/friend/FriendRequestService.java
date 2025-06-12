package com.hungtd.chatapp.service.friend;

import com.hungtd.chatapp.dto.request.FriendRequestRequest;
import com.hungtd.chatapp.dto.response.FriendRequestResponse;

import java.util.List;

public interface FriendRequestService {
    FriendRequestResponse sendFriendRequest(FriendRequestRequest friendRequestRequest);
    FriendRequestResponse acceptFriendRequest(Long requestId);
    void rejectFriendRequest(Long requestId);
    List<FriendRequestResponse> getPendingRequests();
    List<FriendRequestResponse> getSentRequests();
}
