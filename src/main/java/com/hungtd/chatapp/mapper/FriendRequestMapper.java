package com.hungtd.chatapp.mapper;

import com.hungtd.chatapp.dto.response.FriendRequestResponse;
import com.hungtd.chatapp.entity.FriendRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface FriendRequestMapper {
    @Mapping(source = "sender", target = "sender")
    @Mapping(source = "receiver", target = "receiver")
    FriendRequestResponse toFriendRequestResponse(FriendRequest friendRequest);
}
