package com.hungtd.chatapp.service.chat;

import com.hungtd.chatapp.dto.request.MessageRequest;
import com.hungtd.chatapp.entity.Message;

public interface ChatService {
    Message sendMessage(MessageRequest messageRequest);
}
