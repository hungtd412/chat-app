package com.hungtd.chatapp.service.chat.impl;

import com.hungtd.chatapp.dto.request.MessageRequest;
import com.hungtd.chatapp.dto.response.MessageResponse;
import com.hungtd.chatapp.entity.*;
import com.hungtd.chatapp.enums.ErrorCode;
import com.hungtd.chatapp.exception.AppException;
import com.hungtd.chatapp.mapper.MessageMapper;
import com.hungtd.chatapp.repository.MessageRepository;
import com.hungtd.chatapp.repository.UserRepository;
import com.hungtd.chatapp.service.chat.MessageService;
import com.hungtd.chatapp.service.conversation.ConversationService;
import com.hungtd.chatapp.service.user.UserService;
import com.hungtd.chatapp.service.websocket.WebSocketService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageServiceImpl implements MessageService {

    UserService userService;
    MessageMapper messageMapper;
    MessageRepository messageRepository;
    ConversationService conversationService;
    WebSocketService webSocketService;

    @Override
    public List<Message> getMessagesByConversationId(Long conversationId) {
        if (!conversationService.isExistById(conversationId)) {
            throw new AppException(ErrorCode.CONVERSATION_NOT_FOUND);
        }

        validateUserInConversation(conversationId, userService.currentUser().getId());

        return messageRepository.findAllByConversationIdOrderByIdDesc(conversationId);
    }

    public List<MessageResponse> toMessageResponseList(List<Message> messageList) {
        Long currentUserId = userService.currentUser().getId();

        return messageList.stream()
                .map(message -> {
                    User sender = userService.getUserWithNameAndAvt(message.getSenderId());

                    return messageMapper.toMessageResponse(
                            message,
                            message.getConversation().getId(),
                            sender.getFirstName() + " " + sender.getLastName(),
                            sender.getAvtUrl(),
                            message.getSenderId().equals(currentUserId)
                    );
                })
                .toList();
    }

    @Override
    @Transactional
    public void processChatMessage(MessageRequest messageRequest, StompHeaderAccessor headerAccessor) {
        //Flow: get user information -> check user in the conversation
        // -> send websocket -> save

        //a send-message websocket request have jwt token in its header
        //we get the user sending message based on it
        User currentUser = getUserFromStompHeader(headerAccessor);

        Conversation conversation = conversationService.getConversationById(messageRequest.getConversationId());

        validateUserInConversation(conversation.getId(), currentUser.getId());

        Message message = messageMapper.toMessage(messageRequest, conversation, currentUser.getId());

        // Save message to database
        messageRepository.save(message);

        // Send message via WebSocket service
        webSocketService.sendMessage(message, conversation, currentUser);
    }

    private User getUserFromStompHeader(StompHeaderAccessor headerAccessor) {
        String username = webSocketService.extractUsernameFromHeader(headerAccessor);

        return userService.findByUsername(username);
    }

    private void validateUserInConversation(Long conversationId, Long userId) {
        if (!conversationService.isUserInConversation(conversationId, userId)) {
            throw new AppException(ErrorCode.USER_NOT_IN_CONVERSATION);
        }
    }
}