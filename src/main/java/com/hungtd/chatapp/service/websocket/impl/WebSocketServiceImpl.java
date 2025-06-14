package com.hungtd.chatapp.service.websocket.impl;

import com.hungtd.chatapp.dto.response.MessageResponse;
import com.hungtd.chatapp.entity.Conversation;
import com.hungtd.chatapp.entity.Message;
import com.hungtd.chatapp.entity.User;
import com.hungtd.chatapp.enums.ErrorCode;
import com.hungtd.chatapp.exception.AppException;
import com.hungtd.chatapp.mapper.MessageMapper;
import com.hungtd.chatapp.repository.ParticipantRepository;
import com.hungtd.chatapp.repository.UserRepository;
import com.hungtd.chatapp.service.auth.JwtService;
import com.hungtd.chatapp.service.websocket.WebSocketService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebSocketServiceImpl implements WebSocketService {

    JwtService jwtService;
    MessageMapper messageMapper;
    SimpMessagingTemplate messagingTemplate;
    ParticipantRepository participantRepository;
    UserRepository userRepository;

    @Override
    public String extractUsernameFromHeader(StompHeaderAccessor headerAccessor) {
        return jwtService.extractUsernameByTokenStompHeader(headerAccessor);
    }

    @Override
    public void sendWebSocketMessages(Message message, Conversation conversation, Long currentUserId, String currentUsername) {
        if (conversation.getType() == Conversation.Type.PRIVATE || conversation.getType() == Conversation.Type.GROUP) {
            sendMessage(message, conversation.getId(), currentUserId, currentUsername);
        } else {
            throw new AppException(ErrorCode.INVALID_CONVERSATION_TYPE);
        }
    }

    @Override
    public List<String> findOtherUsernamesInConversation(Long conversationId, Long currentUserId) {
        List<Long> otherUserIds = participantRepository.findUserIdsByConversationId(conversationId).stream()
                .filter(id -> !id.equals(currentUserId))
                .collect(Collectors.toList());
        
        if (otherUserIds.isEmpty()) {
            throw new AppException(ErrorCode.RECEIVER_NOT_FOUND);
        }
        
        List<User> receivers = userRepository.findAllById(otherUserIds);
        
        if (receivers.isEmpty()) {
            throw new AppException(ErrorCode.RECEIVER_NOT_FOUND);
        }
        
        return receivers.stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
    }

    private void sendMessage(Message message, Long conversationId,Long currentUserId, String currentUsername) {
        List<String> receiverUsernames = findOtherUsernamesInConversation(conversationId, currentUserId);
        
        // Use the mapper to create message responses
        MessageResponse senderMessage = messageMapper.toMessageResponse(message, conversationId, true);
        MessageResponse receiverMessage = messageMapper.toMessageResponse(message, conversationId, false);
        
        try {
            // Send to the sender for confirmation
            messagingTemplate.convertAndSendToUser(
                    currentUsername,
                    "/queue/messages",
                    senderMessage
            );

            // Send to all receivers
            for (String receiverUsername : receiverUsernames) {
                messagingTemplate.convertAndSendToUser(
                        receiverUsername,
                        "/queue/messages",
                        receiverMessage
                );
            }
        } catch (MessagingException messagingException) {
            log.error("Failed to send WebSocket message", messagingException);
            throw new AppException(ErrorCode.MESSAGE_SENDING_ERROR);
        }
    }
}