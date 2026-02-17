package com.groupy.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.groupy.dto.ChatRequest;
import com.groupy.dto.ConversationHistoryResponse;
import com.groupy.dto.JoinEventResponse;
import com.groupy.dto.MessageResponse;
import com.groupy.dto.ReadReceiptResponse;
import com.groupy.dto.TypingRequest;
import com.groupy.dto.TypingResponse;
import com.groupy.dto.UserStatusResponse;
import com.groupy.entity.Conversation;
import com.groupy.entity.Message;
import com.groupy.entity.User;
import com.groupy.repository.ConversationRepository;
import com.groupy.repository.MessageRepository;
import com.groupy.repository.UserRepository;
import com.groupy.service.PresenceService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final PresenceService presenceService;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatRequest request,
                            Principal principal) {

        User sender = userRepository
                .findByUsername(principal.getName())
                .orElseThrow();

        Conversation conversation =
                conversationRepository.findById(request.getConversationId())
                        .orElseThrow();

        String preview;

        if (request.getType().equals("IMAGE")) {
            preview = "📷 Photo";
        } else if (request.getType().equals("VIDEO")) {
            preview = "🎥 Video";
        } else {
            preview = request.getContent();
        }
        
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(preview)
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

    	User user = userRepository
                .findByUsername(principal.getName())
                .orElseThrow();

        Conversation conversation =
                conversationRepository.findById(request.getConversationId())
                        .orElseThrow();

        // Fetch messages
        List<Message> messages =
                messageRepository.findByConversationIdOrderByCreatedAtAsc(
                        conversation.getId()
                );

        // Convert to MessageResponse
        List<MessageResponse> messageResponses = messages.stream()
                .map(message -> MessageResponse.builder()
                        .id(message.getId().toString())
                        .conversationId(conversation.getId().toString())
                        .senderId(message.getSender().getId().toString())
                        .senderUsername(message.getSender().getUsername())
                        .senderProfilePicture(message.getSender().getImageUrl())
                        .content(message.getContent())
                        .type(message.getType())
                        .createdAt(message.getCreatedAt().toString())
                        .isRead(message.isRead())
                        .build()
                ).toList();

        ConversationHistoryResponse historyResponse =
                new ConversationHistoryResponse(
                        conversation.getId().toString(),
                        messageResponses,
                        "HISTORY"
                );

        // Send ONLY to that user
        messagingTemplate.convertAndSendToUser(
                user.getUsername(),
                "/queue/conversation-history",
                historyResponse
        );

        // Optional: notify others that user joined
        JoinEventResponse joinEvent = new JoinEventResponse(
                conversation.getId(),
                user.getUsername(),
                "JOIN"
        );

//        messagingTemplate.convertAndSend(
//                "/topic/conversation/" + conversation.getId(),
//                joinEvent
//        );
    }
    
    @MessageMapping("/chat.typing")
    public void typing(TypingRequest request,
                       Principal principal) {
    	
        String username = principal.getName();

        TypingResponse response = TypingResponse.builder()
                .conversationId(request.getConversationId().toString())
                .userName(username)
                .typing(request.isTyping())
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

        ReadReceiptResponse response = new ReadReceiptResponse(
                request.getConversationId(),
                user.getId(),
                user.getUsername(),
                "READ"
        );

        messagingTemplate.convertAndSend(
                "/topic/conversation/" + request.getConversationId() + "/read",
                response
        );
    }
    
    @MessageMapping("/presence.sync")
    public void syncPresence(Principal principal) {

    	try {

            if (principal == null) {
                return;
            }

            String username = principal.getName();

            Optional<User> optionalUser =
                    userRepository.findByUsername(username);

            if (optionalUser.isEmpty()) {
                return;
            }

            User currentUser = optionalUser.get();

            List<String> participantsUsername =
                    conversationRepository
                            .findParticipantUsernames(currentUser.getId());

            for (String userName : participantsUsername) {

                boolean online =
                        presenceService.isOnline(userName);

                messagingTemplate.convertAndSendToUser(
                        username,
                        "/queue/presence-sync",
                        new UserStatusResponse(
                        		userName,
                                online
                        )
                );
            }

        } catch (Exception e) {
            e.printStackTrace(); // IMPORTANT for debugging
        }
    }

}