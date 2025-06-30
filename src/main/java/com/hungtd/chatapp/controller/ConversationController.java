package com.hungtd.chatapp.controller;

import com.hungtd.chatapp.dto.request.UpdateGroupTitleRequest;
import com.hungtd.chatapp.dto.request.UploadImageRequest;
import com.hungtd.chatapp.dto.response.ApiResponse;
import com.hungtd.chatapp.dto.response.ConversationResponse;
import com.hungtd.chatapp.entity.Conversation;
import com.hungtd.chatapp.entity.User;
import com.hungtd.chatapp.mapper.ConversationMapper;
import com.hungtd.chatapp.service.conversation.ConversationService;
import com.hungtd.chatapp.service.user.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationController {

    ConversationService conversationService;
    UserService userService;
    private final ConversationMapper conversationMapper;

    @GetMapping("/current-user")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getCurrentUserConversations() {
        List<Conversation> conversations = conversationService.getCurrentUserConversations();
        User currentUser = userService.currentUser();
        
        List<ConversationResponse> conversationResponses = conversations.stream()
                .map(conversation -> conversationService.enrichConversationWithFriendNameAngImage(conversation, currentUser.getId()))
                .toList();
                
        return ResponseEntity.ok(
                ApiResponse.<List<ConversationResponse>>builder()
                        .data(conversationResponses)
                        .build()
        );
    }

    @PatchMapping("/{id}/title")
    public ResponseEntity<ApiResponse<ConversationResponse>> updateGroupTitle(
            @PathVariable("id") Long id,
            @Valid UpdateGroupTitleRequest updateGroupTitleRequest
    ) {
        Conversation conversation = conversationService.updateGroupTitle(id, updateGroupTitleRequest);

        return ResponseEntity.ok(
                ApiResponse.<ConversationResponse>builder()
                        .data(conversationMapper.toConversationResponse(conversation))
                        .build()
        );
    }

    @PatchMapping("/{id}/image")
    public ResponseEntity<ApiResponse<ConversationResponse>> updateGroupImage(
            @PathVariable("id") Long id,
            @Valid UploadImageRequest uploadImageRequest
    ) {
        Conversation conversation = conversationService.updateGroupImg(id, uploadImageRequest);

        return ResponseEntity.ok(
                ApiResponse.<ConversationResponse>builder()
                        .data(conversationMapper.toConversationResponse(conversation))
                        .build()
        );
    }

}
