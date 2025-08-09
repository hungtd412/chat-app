package com.hungtd.chatapp.mapper;

import com.hungtd.chatapp.dto.response.ConversationResponse;
import com.hungtd.chatapp.entity.Conversation;
import com.hungtd.chatapp.projection.ConversationProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConversationMapper {
    
    @Mapping(target = "friendName", ignore = true)
    ConversationResponse toConversationResponse(Conversation conversation);


    @Mapping(target = "imageUrl", source = "conversationProjection.image_url")
    @Mapping(target = "friendName", source = "conversationProjection.friend_name")
    @Mapping(target = "avtUrl", source = "conversationProjection.avt_url")
    @Mapping(target = "latestMessage", source = "conversationProjection.latest_message")
    @Mapping(target = "createdAt", source = "conversationProjection.created_at")
    ConversationResponse toConversationResponse(ConversationProjection conversationProjection);
}
