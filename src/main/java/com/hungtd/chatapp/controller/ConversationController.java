package com.hungtd.chatapp.controller;

import com.hungtd.chatapp.dto.response.ApiResponse;
import com.hungtd.chatapp.dto.response.ConversationResponse;
import com.hungtd.chatapp.entity.Conversation;
import com.hungtd.chatapp.entity.User;
import com.hungtd.chatapp.service.conversation.ConversationService;
import com.hungtd.chatapp.service.user.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationController {

    ConversationService conversationService;
    UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getCurrentUserConversations() {
        List<Conversation> conversations = conversationService.getCurrentUserConversations();
        User currentUser = userService.currentUser();
        
        List<ConversationResponse> conversationResponses = conversations.stream()
                .map(conversation -> conversationService.enrichConversationWithFriendName(conversation, currentUser.getId()))
                .toList();
                
        return ResponseEntity.ok(
                ApiResponse.<List<ConversationResponse>>builder()
                        .data(conversationResponses)
                        .build()
        );
    }
}
