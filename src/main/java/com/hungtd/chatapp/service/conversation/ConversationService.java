package com.hungtd.chatapp.service.conversation;

import com.hungtd.chatapp.dto.request.UpdateGroupTitleRequest;
import com.hungtd.chatapp.dto.request.UploadImageRequest;
import com.hungtd.chatapp.dto.response.ConversationResponse;
import com.hungtd.chatapp.entity.Conversation;

import java.util.List;

public interface ConversationService {

    void create(Conversation.Type conversationType, String conversationTitle, List<Long> participantIds);

    boolean isExistById(Long conversationId);

    Conversation getConversationById(Long conversationId);

    List<Conversation> getCurrentUserConversations();

    ConversationResponse enrichConversationWithFriendNameAngImage(Conversation conversation, Long currentUserId);

    Conversation updateGroupTitle(Long conversationId, UpdateGroupTitleRequest updateGroupTitleRequest);

    Conversation updateGroupImg(Long conversationId, UploadImageRequest uploadImageRequest);

    public void validateUserInConversation(Long conversationId, Long userId);
}
