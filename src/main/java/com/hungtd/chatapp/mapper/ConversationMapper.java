package com.hungtd.chatapp.mapper;

import com.hungtd.chatapp.dto.response.ConversationResponse;
import com.hungtd.chatapp.entity.Conversation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConversationMapper {
    
    @Mapping(target = "friendName", ignore = true)
    ConversationResponse toConversationResponse(Conversation conversation);
}
