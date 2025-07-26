package com.hungtd.chatapp.repository;

import com.hungtd.chatapp.dto.response.UserNameAndAvatarResponse;
import com.hungtd.chatapp.entity.Friend;
import com.hungtd.chatapp.entity.FriendId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

    @Query(value = "SELECT u.id, CONCAT(u.first_name, ' ', u.last_name) as friendName, u.avt_url as avtUrl, " +
            "    (SELECT p.conversation_id " +
            "    FROM participants p " +
            "    JOIN conversations c ON p.conversation_id = c.id " +
            "    WHERE (p.user_id = :userId OR p.user_id = u.id) and c.type = 'PRIVATE' " +
            " GROUP BY p.conversation_id " +
            " HAVING COUNT(DISTINCT p.user_id) = 2) as conversationId " +
            " FROM friends f " +
            " JOIN users u ON (CASE WHEN f.user_id_1 = :userId THEN " +
            " f.user_id_2 ELSE f.user_id_1 END) = u.id " +
            " WHERE f.user_id_1 = :userId OR f.user_id_2 = :userId ",
            countQuery = "SELECT COUNT(*) FROM friends f " +
                    "WHERE f.user_id_1 = :userId OR f.user_id_2 = :userId",
            nativeQuery = true)
    Page<UserNameAndAvatarResponse> findFriendsWithDetailsPaged(
            @Param("userId") Long userId, Pageable pageable);
}