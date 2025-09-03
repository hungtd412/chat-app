package com.hungtd.chatapp.controller;

import com.hungtd.chatapp.dto.request.UpdateGroupTitleRequest;
import com.hungtd.chatapp.dto.response.ApiResponse;
import com.hungtd.chatapp.dto.response.ConversationResponse;
import com.hungtd.chatapp.entity.Conversation;
import com.hungtd.chatapp.mapper.ConversationMapper;
import com.hungtd.chatapp.service.conversation.ConversationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationController {

    ConversationService conversationService;
    ConversationMapper conversationMapper;

    @GetMapping("/current-user")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getCurrentUserConversations() {
        List<ConversationResponse> conversationResponses = conversationService.getCurrentUserConversations();

        return ResponseEntity.ok(
                ApiResponse.<List<ConversationResponse>>builder()
                        .data(conversationResponses)
                        .build()
        );
    }

    @PatchMapping("/{id}/title")
    public ResponseEntity<ApiResponse<ConversationResponse>> updateGroupTitle(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateGroupTitleRequest updateGroupTitleRequest
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
            @RequestPart("image") MultipartFile image
    ) {
        Conversation conversation = conversationService.updateGroupImg(id, image);

        return ResponseEntity.ok(
                ApiResponse.<ConversationResponse>builder()
                        .data(conversationMapper.toConversationResponse(conversation))
                        .build()
        );
    }

}
