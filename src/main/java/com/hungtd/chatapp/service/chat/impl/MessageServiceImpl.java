package com.hungtd.chatapp.service.chat.impl;

import com.hungtd.chatapp.dto.request.MessageRequest;
import com.hungtd.chatapp.dto.response.MessageResponse;
import com.hungtd.chatapp.entity.*;
import com.hungtd.chatapp.enums.ErrorCode;
import com.hungtd.chatapp.exception.AppException;
import com.hungtd.chatapp.repository.ConversationRepository;
import com.hungtd.chatapp.repository.MessageRepository;
import com.hungtd.chatapp.repository.ParticipantRepository;
import com.hungtd.chatapp.service.chat.MessageService;
import com.hungtd.chatapp.service.user.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageServiceImpl implements MessageService {

    UserService userService;

    MessageRepository messageRepository;
    ConversationRepository conversationRepository;
    ParticipantRepository participantRepository;

    SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public Message sendMessage(MessageRequest messageRequest) {
        Long senderId = userService.currentUser().getId();
        Long conversationId = messageRequest.getConversationId();
        
        // Validate conversation and user participation
        Conversation conversation = validateConversationAndParticipation(conversationId, senderId);
        
        // Create and save message
        Message message = Message.builder()
                .conversation(conversation)
                .senderId(senderId)
                .message(messageRequest.getMessage())
                .type(messageRequest.getType())
                .build();
        
        message = messageRepository.save(message);
        
        // Send notification based on conversation type
        if (conversation.getType() == Conversation.Type.PRIVATE) {
            // For private chats, get the other participant directly
            // Since private chat is one-to-one, we can just get the other user ID
            Long receiverId = participantRepository.findUserIdsByConversationId(conversationId).stream()
                .filter(userId -> !userId.equals(senderId))
                .findFirst()
                .orElse(null);

            if (receiverId != null) {
                // Send to the other user's queue
                messagingTemplate.convertAndSendToUser(
                    receiverId.toString(),
                    "/queue/messages",
                    message
                );
            }
        } else {
            // For group chats, broadcast to topic
            messagingTemplate.convertAndSend("/topic/chat/" + conversationId, message);
        }
        
        return message;
    }

    private Conversation validateConversationAndParticipation(Long conversationId, Long userId) {
        // Validate conversation exists
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
        
        // Validate if sender is a participant in the conversation
        ParticipantId participantId = new ParticipantId(conversationId, userId);
        if (!participantRepository.existsById(participantId)) {
            throw new AppException(ErrorCode.USER_NOT_IN_CONVERSATION);
        }
        
        return conversation;
    }

    @Override
    public List<Message> getMessagesByConversationId(Long conversationId) {
        // Get current user
        Long userId = userService.currentUser().getId();
        
        // Validate conversation and user participation
        validateConversationAndParticipation(conversationId, userId);
        
        // Fetch messages from repository ordered by ID (newest first)
        return messageRepository.findAllByConversationIdOrderByIdDesc(conversationId);
    }
}