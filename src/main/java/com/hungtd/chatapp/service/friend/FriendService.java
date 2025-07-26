package com.hungtd.chatapp.service.friend;

import com.hungtd.chatapp.dto.request.FriendRequestRequest;
import com.hungtd.chatapp.dto.response.FriendRequestResponse;
import com.hungtd.chatapp.dto.response.UserNameAndAvatarResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface FriendService {
    FriendRequestResponse sendFriendRequest(FriendRequestRequest friendRequestRequest);
    FriendRequestResponse acceptFriendRequest(Long requestId);
    void rejectFriendRequest(Long requestId);
    List<FriendRequestResponse> getPendingRequests();
    List<FriendRequestResponse> getSentRequests();
    Page<UserNameAndAvatarResponse> getAllFriends(int page, int size);
}
