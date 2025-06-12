package com.hungtd.chatapp.repository;

import com.hungtd.chatapp.entity.FriendRequest;
import com.hungtd.chatapp.enums.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findBySender_Id(Long senderId);
    List<FriendRequest> findByReceiver_Id(Long receiverId);
    Optional<FriendRequest> findBySender_IdAndReceiver_Id(Long senderId, Long receiverId);
    List<FriendRequest> findByReceiver_IdAndStatus(Long receiverId, FriendRequestStatus status);
}
