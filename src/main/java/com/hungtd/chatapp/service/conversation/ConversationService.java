package com.hungtd.chatapp.service.conversation;

import com.hungtd.chatapp.dto.response.ConversationResponse;
import com.hungtd.chatapp.entity.Conversation;
import java.util.List;

public interface ConversationService {

    void create(Conversation.Type conversationType, String conversationTitle, List<Long> participantIds);

    boolean isExistById(Long conversationId);

    boolean isUserInConversation(Long conversationId, Long userId);

    Conversation getConversationById(Long conversationId);

    List<Conversation> getCurrentUserConversations();

    ConversationResponse enrichConversationWithFriendName(Conversation conversation, Long currentUserId);
}
