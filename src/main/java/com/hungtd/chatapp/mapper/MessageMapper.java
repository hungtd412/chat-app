package com.hungtd.chatapp.mapper;

import com.hungtd.chatapp.dto.request.MessageRequest;
import com.hungtd.chatapp.dto.response.MessageResponse;
import com.hungtd.chatapp.entity.Conversation;
import com.hungtd.chatapp.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    
    /**
     * Maps a Message entity to a MessageResponse DTO with conversation ID and ownership info
     *
     * @param message the source Message entity
     * @param conversationId the ID of the conversation
     * @param isBelongCurrentUser whether the message belongs to the current user
     * @return the mapped MessageResponse DTO
     */
    @Mapping(target = "conversationId", source = "conversationId")
    @Mapping(target = "isBelongCurrentUser", source = "isBelongCurrentUser")
    @Mapping(target = "content", source = "message.message") // Map message.message to content
    MessageResponse toMessageResponse(Message message, Long conversationId, boolean isBelongCurrentUser);
    
    /**
     * Maps a MessageRequest to a Message entity
     *
     * @param messageRequest the source MessageRequest DTO
     * @param conversation the conversation associated with the message
     * @param senderId the ID of the user sending the message
     * @return the mapped Message entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conversation", source = "conversation")
    @Mapping(target = "senderId", source = "senderId")
    @Mapping(target = "message", source = "messageRequest.content") // Map messageRequest.content to message
    @Mapping(target = "type", source = "messageRequest.type")
    Message toMessage(MessageRequest messageRequest, Conversation conversation, Long senderId);

    /**
     * Maps a list of Message entities to MessageResponse DTOs with current user ownership information
     * 
     * @param messages the list of Message entities to map
     * @param currentUserId the ID of the current user to determine message ownership
     * @return the list of mapped MessageResponse DTOs
     */
    default List<MessageResponse> toMessageResponseList(List<Message> messages, Long currentUserId) {
        return messages.stream()
                .map(message -> toMessageResponse(
                        message,
                        message.getConversation().getId(),
                        message.getSenderId().equals(currentUserId)
                ))
                .toList();
    }
}