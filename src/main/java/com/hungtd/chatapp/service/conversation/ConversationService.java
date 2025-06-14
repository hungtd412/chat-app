package com.hungtd.chatapp.service.conversation;

import com.hungtd.chatapp.dto.response.ConversationResponse;
import com.hungtd.chatapp.entity.Conversation;
import java.util.List;

public interface ConversationService {

    List<Conversation> getCurrentUserConversations();

    ConversationResponse enrichConversationWithFriendName(Conversation conversation, Long currentUserId);
}
