package com.hungtd.chatapp.service.conversation;

import com.hungtd.chatapp.dto.request.UpdateGroupTitleRequest;
import com.hungtd.chatapp.dto.request.UploadImageRequest;
import com.hungtd.chatapp.dto.response.ConversationResponse;
import com.hungtd.chatapp.entity.Conversation;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ConversationService {

    void create(Conversation.Type conversationType, String conversationTitle, List<Long> participantIds);

    boolean isExistById(Long conversationId);

    Conversation getConversationById(Long conversationId);

    List<ConversationResponse> getCurrentUserConversations();

    /**
     *
     * @param conversation
     * @param currentUserId
     * @return a list of conversations, each conversation has their friend avt, friend name
     * and latest message
     */
    ConversationResponse enrichConversation(Conversation conversation, Long currentUserId);

    Conversation updateGroupTitle(Long conversationId, UpdateGroupTitleRequest updateGroupTitleRequest);

    Conversation updateGroupImg(Long conversationId, MultipartFile image);

    public void validateUserInConversation(Long conversationId, Long userId);
}
