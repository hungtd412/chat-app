package com.hungtd.chatapp.service.conversation.impl;

import com.hungtd.chatapp.dto.response.ConversationResponse;
import com.hungtd.chatapp.entity.Conversation;
import com.hungtd.chatapp.entity.Participant;
import com.hungtd.chatapp.entity.User;
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
    public List<Conversation> getCurrentUserConversations() {
        User currentUser = userService.currentUser();
        return conversationRepository.findAllByUserId(currentUser.getId());
    }
    
    @Override
    public ConversationResponse enrichConversationWithFriendName(Conversation conversation, Long currentUserId) {
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
                }
            }
        }
        
        return response;
    }
}
