package com.hungtd.chatapp.mapper;

import com.hungtd.chatapp.dto.response.MessageResponse;
import com.hungtd.chatapp.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    
    @Mapping(target = "content", source = "message")
    @Mapping(target = "isBelongCurrentUser", ignore = true)
    MessageResponse toMessageResponse(Message message);
}
