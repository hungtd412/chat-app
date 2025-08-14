package com.hungtd.chatapp.service.friend.impl;

import com.hungtd.chatapp.dto.request.FriendRequestRequest;
import com.hungtd.chatapp.dto.response.FriendRequestResponse;
import com.hungtd.chatapp.dto.response.UserNameAndAvatarResponse;
import com.hungtd.chatapp.entity.Conversation;
import com.hungtd.chatapp.entity.Friend;
import com.hungtd.chatapp.entity.FriendRequest;
import com.hungtd.chatapp.entity.User;
import com.hungtd.chatapp.enums.ErrorCode;
import com.hungtd.chatapp.enums.FriendRequestStatus;
import com.hungtd.chatapp.exception.AppException;
import com.hungtd.chatapp.mapper.FriendRequestMapper;
import com.hungtd.chatapp.repository.FriendRepository;
import com.hungtd.chatapp.repository.FriendRequestRepository;
import com.hungtd.chatapp.repository.UserRepository;
import com.hungtd.chatapp.service.conversation.ConversationService;
import com.hungtd.chatapp.service.friend.FriendService;
import com.hungtd.chatapp.service.user.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FriendServiceImpl implements FriendService {

    FriendRequestRepository friendRequestRepository;
    UserRepository userRepository;
    FriendRequestMapper friendRequestMapper;
    FriendRepository friendRepository;

    ConversationService conversationService;
    UserService userService;

    @Override
    public Page<UserNameAndAvatarResponse> getAllFriends(int page, int size) {
        User currentUser = userService.currentUser();
        Long userId = currentUser.getId();

        // Don't specify a sort property since the sorting is handled in the JPQL query
        Pageable pageable = PageRequest.of(page, size, Sort.by("friendName"));

        // Get paginated friends with details directly from database
        return friendRepository.findFriendsWithDetailsPaged(userId, pageable);
    }

    @Override
    @Transactional
    public FriendRequestResponse sendFriendRequest(FriendRequestRequest friendRequestRequest) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User sender = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        User receiver = userRepository.findById(friendRequestRequest.getReceiverId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        // Check if the request is not to self
        if (Objects.equals(sender.getId(), receiver.getId())) {
            throw new AppException(ErrorCode.CANNOT_SEND_FRIEND_REQUEST_TO_SELF);
        }
        
        // Check if friend request already exists
        friendRequestRepository.findBySender_IdAndReceiver_Id(sender.getId(), receiver.getId())
                .ifPresent(request -> {
                    throw new AppException(ErrorCode.FRIEND_REQUEST_ALREADY_SENT);
                });
        
        // Check if the receiver has already sent a request to the sender
        friendRequestRepository.findBySender_IdAndReceiver_Id(receiver.getId(), sender.getId())
                .ifPresent(request -> {
                    throw new AppException(ErrorCode.FRIEND_REQUEST_ALREADY_RECEIVED);
                });
        
        FriendRequest friendRequest = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.PENDING)
                .build();
        
        FriendRequest saved = friendRequestRepository.save(friendRequest);
        return friendRequestMapper.toFriendRequestResponse(saved);
    }

    @Override
    @Transactional
    public FriendRequestResponse acceptFriendRequest(Long requestId) {
        User currentUser = userService.currentUser();
        
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        validateFriendRequest(friendRequest, currentUser);
        
        friendRequest.setStatus(FriendRequestStatus.ACCEPTED);
        FriendRequest saved = friendRequestRepository.save(friendRequest);

        createFriendship(currentUser.getId(), friendRequest.getSender().getId());

        conversationService.create(
                Conversation.Type.PRIVATE,
                "",
                Arrays.asList(currentUser.getId(), friendRequest.getSender().getId())
        );
        
        return friendRequestMapper.toFriendRequestResponse(saved);
    }

    @Override
    @Transactional
    public void rejectFriendRequest(Long requestId) {
        User currentUser = userService.currentUser();
        
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));
        
        // Verify that the current user is the receiver of the request
        if (!Objects.equals(friendRequest.getReceiver().getId(), currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        // Delete the friend request
        friendRequestRepository.delete(friendRequest);
    }

    @Override
    public List<FriendRequestResponse> getPendingRequests() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        List<FriendRequest> pendingRequests = friendRequestRepository
                .findByReceiver_IdAndStatus(currentUser.getId(), FriendRequestStatus.PENDING);
        
        return pendingRequests.stream()
                .map(friendRequestMapper::toFriendRequestResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendRequestResponse> getSentRequests() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        List<FriendRequest> sentRequests = friendRequestRepository.findBySender_Id(currentUser.getId());
        
        return sentRequests.stream()
                .map(friendRequestMapper::toFriendRequestResponse)
                .collect(Collectors.toList());
    }

    private void validateFriendRequest(FriendRequest friendRequest, User currentUser) {
        // Verify that the current user is the receiver of the request
        if (!Objects.equals(friendRequest.getReceiver().getId(), currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Check if the request is still pending
        if (friendRequest.getStatus() != FriendRequestStatus.PENDING) {
            throw new AppException(ErrorCode.FRIEND_REQUEST_ALREADY_PROCESSED);
        }
    }

    private void createFriendship(Long userId1, Long userId2) {
        friendRepository.save(
                Friend.builder()
                        .userId1(userId1)
                        .userId2(userId2)
                        .build()
        );
    }
}