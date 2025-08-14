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
    public Long extractUserIdFromHeader(StompHeaderAccessor headerAccessor) {
        return jwtService.extractUserIdByTokenStompHeader(headerAccessor);
    }

    @Override
    public void sendMessage(Message message, Conversation conversation, User currentUser) {

        List<User> receivers = findOtherUsersInConversation(conversation.getId(), currentUser.getId());

        // Use the mapper to create message responses
        MessageResponse senderMessage = messageMapper.toMessageResponse(
                message,
                conversation.getId(),
                currentUser.getFirstName() + " " + currentUser.getLastName(),
                currentUser.getAvtUrl(),
                true
        );

        MessageResponse receiverMessage = messageMapper.toMessageResponse(
                message,
                conversation.getId(),
                currentUser.getFirstName() + " " + currentUser.getLastName(),
                currentUser.getAvtUrl(),
                false
        );

        try {
            // Send to the sender for confirmation
            messagingTemplate.convertAndSendToUser(
                    currentUser.getId().toString(),
                    "/queue/messages",
                    senderMessage
            );

            // Send to all receivers
            for (User user : receivers) {
                messagingTemplate.convertAndSendToUser(
                        user.getId().toString(),
                        "/queue/messages",
                        receiverMessage
                );
            }
        } catch (MessagingException messagingException) {
            log.error("Failed to send WebSocket message", messagingException);
            throw new AppException(ErrorCode.MESSAGE_SENDING_ERROR);
        }
    }

    @Override
    public List<User> findOtherUsersInConversation(Long conversationId, Long currentUserId) {
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

        return receivers;
    }
}