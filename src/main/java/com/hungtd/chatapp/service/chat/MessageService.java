package com.hungtd.chatapp.service.chat;

import com.hungtd.chatapp.dto.request.MessageRequest;
import com.hungtd.chatapp.dto.response.MessageResponse;
import com.hungtd.chatapp.entity.Message;

import java.util.List;

public interface MessageService {
    Message sendMessage(MessageRequest messageRequest);
    List<Message> getMessagesByConversationId(Long conversationId);
}
