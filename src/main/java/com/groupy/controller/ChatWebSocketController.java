package com.groupy.controller;

import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.groupy.dto.ChatRequest;
import com.groupy.dto.MessageResponse;
import com.groupy.dto.TypingRequest;
import com.groupy.dto.TypingResponse;
import com.groupy.entity.Conversation;
import com.groupy.entity.Message;
import com.groupy.entity.User;
import com.groupy.repository.ConversationRepository;
import com.groupy.repository.MessageRepository;
import com.groupy.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatRequest request,
                            Principal principal) {

        User sender = userRepository
                .findByUsername(principal.getName())
                .orElseThrow();

        Conversation conversation =
                conversationRepository.findById(request.getConversationId())
                        .orElseThrow();

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.getContent())
                .type(request.getType())
                .build();

        messageRepository.save(message);

        conversation.setLastMessage(request.getContent());
        conversation.setLastMessageTime(LocalDateTime.now());
        conversationRepository.save(conversation);

        MessageResponse response = MessageResponse.builder()
                .id(message.getId().toString())
                .conversationId(conversation.getId().toString())
                .senderId(sender.getId().toString())
                .senderUsername(sender.getUsername())
                .senderProfilePicture(sender.getImageUrl())
                .content(message.getContent())
                .type(message.getType())
                .createdAt(message.getCreatedAt().toString())
                .isRead(false)
                .build();
        
        System.out.println(response);

        messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversation.getId(),
                response
        );
    }
    
    @MessageMapping("/chat.join")
    public void joinConversation(ChatRequest request,
                                 Principal principal) {

        String username = principal.getName();

        messagingTemplate.convertAndSend(
                "/topic/conversation/" + request.getConversationId(),
                username + " joined the conversation"
        );
    }
    
    @MessageMapping("/chat.typing")
    public void typing(TypingRequest request,
                       Principal principal) {

        String username = principal.getName();

        TypingResponse response = TypingResponse.builder()
                .conversationId(request.getConversationId().toString())
                .username(username)
                .isTyping(request.isTyping())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/conversation/" + request.getConversationId() + "/typing",
                response
        );
    }
    
    @MessageMapping("/chat.read")
    public void markAsRead(ChatRequest request,
                           Principal principal) {

        User user = userRepository
                .findByUsername(principal.getName())
                .orElseThrow();

        messageRepository.markMessagesAsRead(
                request.getConversationId(),
                user.getId()
        );

        messagingTemplate.convertAndSend(
                "/topic/conversation/" + request.getConversationId() + "/read",
                user.getUsername() + " read messages"
        );
    }



}