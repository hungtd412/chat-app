package com.hungtd.chatapp.controller;

import com.hungtd.chatapp.dto.request.MessageRequest;
import com.hungtd.chatapp.dto.response.ApiResponse;
import com.hungtd.chatapp.entity.Message;
import com.hungtd.chatapp.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("messages")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<Message>> sendMessage(@Valid @RequestBody MessageRequest messageRequest) {
        Message sentMessage = chatService.sendMessage(messageRequest);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<Message>builder()
                        .data(sentMessage)
                        .build()
        );
    }
}
