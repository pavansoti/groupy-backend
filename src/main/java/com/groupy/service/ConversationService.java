package com.groupy.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.groupy.dto.ConversationResponse;
import com.groupy.entity.Conversation;
import com.groupy.entity.User;
import com.groupy.repository.ConversationRepository;
import com.groupy.repository.MessageRepository;
import com.groupy.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final PresenceService presenceService;

    public ConversationResponse createOrGet(Long currentUserId, Long targetUserId) {

    	Conversation conversation = conversationRepository
            .findConversationBetween(currentUserId, targetUserId)
            .orElseGet(() -> {
                User currentUser = userRepository.findById(currentUserId).orElseThrow();
                User targetUser = userRepository.findById(targetUserId).orElseThrow();

                Conversation newConv = new Conversation();
                newConv.getParticipants().add(currentUser);
                newConv.getParticipants().add(targetUser);
                return conversationRepository.save(newConv);
            });

        return mapToResponse(conversation, currentUserId);
    }

    public List<ConversationResponse> getUserConversations(Long userId) {

        List<Conversation> conversations =
                conversationRepository.findByUserId(userId);

        return conversations.stream().map(c -> {
            return mapToResponse(c, userId);
        }).toList();
    }
    
    private ConversationResponse mapToResponse(Conversation c, Long currentUserId) {

        User participant = c.getParticipants().stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .findFirst()
                .orElseThrow();

        int unreadCount = messageRepository
                .countByConversationIdAndSenderIdNotAndIsReadFalse(
                        c.getId(),
                        currentUserId
                );

        return ConversationResponse.builder()
                .id(c.getId().toString())
                .participantId(participant.getId().toString())
                .participantUsername(participant.getUsername())
                .participantProfilePicture(participant.getImageUrl())
                .lastMessage(c.getLastMessage())
                .lastMessageTime(
                        c.getLastMessageTime() != null
                                ? c.getLastMessageTime().toString()
                                : null
                )
                .unreadCount(unreadCount)
                .isOnline(presenceService.isOnline(participant.getUsername()))
                .build();
    }

}
