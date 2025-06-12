package com.hungtd.chatapp.repository;

import com.hungtd.chatapp.entity.Friend;
import com.hungtd.chatapp.entity.FriendId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendRepository extends JpaRepository<Friend, FriendId> {
    
    List<Friend> findByUserId1(Long userId);
    
    List<Friend> findByUserId2(Long userId);
    
    @Query("SELECT f FROM Friend f WHERE f.userId1 = :userId OR f.userId2 = :userId")
    List<Friend> findAllFriendsByUserId(@Param("userId") Long userId);
    
    void deleteByUserId1AndUserId2(Long userId1, Long userId2);
}
