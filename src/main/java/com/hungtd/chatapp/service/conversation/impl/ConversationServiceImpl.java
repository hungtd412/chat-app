package com.hungtd.chatapp.service.conversation.impl;

import com.hungtd.chatapp.dto.response.ConversationResponse;
import com.hungtd.chatapp.entity.Conversation;
import com.hungtd.chatapp.entity.Participant;
import com.hungtd.chatapp.entity.ParticipantId;
import com.hungtd.chatapp.entity.User;
import com.hungtd.chatapp.enums.ErrorCode;
import com.hungtd.chatapp.exception.AppException;
import com.hungtd.chatapp.mapper.ConversationMapper;
import com.hungtd.chatapp.repository.ConversationRepository;
import com.hungtd.chatapp.repository.ParticipantRepository;
import com.hungtd.chatapp.repository.UserRepository;
import com.hungtd.chatapp.service.conversation.ConversationService;
import com.hungtd.chatapp.service.user.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationServiceImpl implements ConversationService {

    ConversationRepository conversationRepository;
    UserService userService;
    ParticipantRepository participantRepository;
    UserRepository userRepository;
    ConversationMapper conversationMapper;


    @Override
    public void create(Conversation.Type conversationType, String conversationTitle,  List<Long> participantIdsList) {
        Conversation conversation = Conversation.builder()
                .title(conversationTitle)
                .type(conversationType)
                .build();

        conversation = conversationRepository.save(conversation);
        final Long conversationId = conversation.getId();

        List<Participant> participantsList = participantIdsList.stream()
                .map(
                        participantId -> Participant.builder()
                        .conversationId(conversationId)
                        .userId(participantId)
                        .type(Participant.Type.MEMBER)
                        .build()
                )
                .collect(Collectors.toList());

        participantRepository.saveAll(participantsList);
    }

    @Override
    public boolean isExistById(Long conversationId) {
        return conversationRepository.existsById(conversationId);
    }

    @Override
    public boolean isUserInConversation(Long conversationId, Long userId) {
        ParticipantId participantId = new ParticipantId(conversationId, userId);
        return participantRepository.existsById(participantId);
    }

    @Override
    public Conversation getConversationById(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
    }

    @Override
    public List<Conversation> getCurrentUserConversations() {
        User currentUser = userService.currentUser();
        return conversationRepository.findAllByUserIdOrderByNewestMessage(currentUser.getId());
    }
    
    @Override
    public ConversationResponse enrichConversationWithFriendNameAngImage(Conversation conversation, Long currentUserId) {
        ConversationResponse response = conversationMapper.toConversationResponse(conversation);
        
        // For private conversations, find the other participant and add their name
        if (conversation.getType() == Conversation.Type.PRIVATE) {
            List<Participant> participants = participantRepository.findOtherParticipants(
                    conversation.getId(), currentUserId);
            
            if (!participants.isEmpty()) {
                Participant otherParticipant = participants.get(0);
                Optional<User> otherUser = userRepository.findById(otherParticipant.getUserId());
                
                if (otherUser.isPresent()) {
                    User friend = otherUser.get();
                    response.setFriendName(friend.getFirstName() + " " + friend.getLastName());
                    response.setImageUrl(friend.getAvtUrl());
                }
            }
        } else if (conversation.getType() == Conversation.Type.GROUP) {
            response.setImageUrl(conversation.getImageUrl());
        } else {
            throw new AppException(ErrorCode.INVALID_CONVERSATION_TYPE);
        }
        
        return response;
    }
}