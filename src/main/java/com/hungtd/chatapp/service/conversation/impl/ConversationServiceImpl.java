package com.hungtd.chatapp.service.conversation.impl;

import com.hungtd.chatapp.configuration.CloudinaryConfig;
import com.hungtd.chatapp.dto.request.UpdateGroupTitleRequest;
import com.hungtd.chatapp.dto.request.UploadImageRequest;
import com.hungtd.chatapp.dto.response.CloudinaryResponse;
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
import com.hungtd.chatapp.service.cloudinary.CloudinaryService;
import com.hungtd.chatapp.service.conversation.ConversationService;
import com.hungtd.chatapp.service.user.UserService;
import com.hungtd.chatapp.util.FileUploadUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ConversationServiceImpl implements ConversationService {

    ConversationRepository conversationRepository;
    UserService userService;
    ParticipantRepository participantRepository;
    UserRepository userRepository;
    ConversationMapper conversationMapper;
    CloudinaryService cloudinaryService;


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

    @Override
    public Conversation updateGroupTitle(Long conversationId, UpdateGroupTitleRequest updateGroupTitleRequest) {
        //validate current user is in the conversation
        validateUserInConversation(conversationId, userService.currentUser().getId());

        Conversation conversation = getConversationById(conversationId);

        if (!isGroupConversation(conversation)) {
            throw new AppException(ErrorCode.GROUP_CONVERSATION_TYPE_REQUIRED);
        }

        conversation.setTitle(updateGroupTitleRequest.getTitle());
        return conversationRepository.save(conversation);
    }

    @Override
    public Conversation updateGroupImg(Long conversationId, UploadImageRequest uploadImageRequest) {
        //validate current user is in the conversation
        validateUserInConversation(conversationId, userService.currentUser().getId());

        Conversation conversation = getConversationById(conversationId);

        if (!isGroupConversation(conversation)) {
            throw new AppException(ErrorCode.GROUP_CONVERSATION_TYPE_REQUIRED);
        }

        FileUploadUtil.assertAllowed(uploadImageRequest.getImage(), FileUploadUtil.IMAGE_PATTERN);


        if (StringUtils.isNotBlank(conversation.getCloudinaryImageId())
                && !(Objects.equals(conversation.getCloudinaryImageId(),
                CloudinaryConfig.CLOUDINARY_DEFAULT_GROUP_PUBLICID))
        ) {
            cloudinaryService.delete(conversation.getCloudinaryImageId());
        }

        final CloudinaryResponse cloudinaryResponse = cloudinaryService.uploadFile(uploadImageRequest.getImage(), "group");

        conversation.setImageUrl(cloudinaryResponse.getUrl());
        conversation.setCloudinaryImageId(cloudinaryResponse.getPublicId());
        return conversationRepository.save(conversation);
    }

    @Override
    public void validateUserInConversation(Long conversationId, Long userId) {
        if (!isUserInConversation(conversationId, userId)) {
            throw new AppException(ErrorCode.USER_NOT_IN_CONVERSATION);
        }
    }

    private boolean isUserInConversation(Long conversationId, Long userId) {
        ParticipantId participantId = new ParticipantId(conversationId, userId);
        return participantRepository.existsById(participantId);
    }

    private boolean isGroupConversation(Conversation conversation) {
        return conversation.getType().equals(Conversation.Type.GROUP);
    }

}