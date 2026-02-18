package com.groupy.websocket;

import java.security.Principal;
import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.groupy.dto.UserStatusResponse;
import com.groupy.entity.User;
import com.groupy.repository.ConversationRepository;
import com.groupy.repository.UserRepository;
import com.groupy.service.PresenceService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        sendPresence(event, true);
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        sendPresence(event, false);
    }

    private void sendPresence(AbstractSubProtocolEvent event, boolean online) {

        try {
            StompHeaderAccessor accessor =
                    StompHeaderAccessor.wrap(event.getMessage());

            Principal principal = accessor.getUser();

            if (principal == null) return;

            String username = principal.getName();

            User user = userRepository.findByUsername(username)
                    .orElse(null);

            if (user == null) return;
            
            if (online) {
                presenceService.userConnected(username);
            } else {
                presenceService.userDisconnected(username);
            }

            List<String> participants =
                    conversationRepository
                            .findParticipantUsernames(user.getId());

            UserStatusResponse payload =
                    new UserStatusResponse(username, online);

            for (String participant : participants) {

                messagingTemplate.convertAndSendToUser(
                        participant,
                        "/queue/presence",
                        payload
                );
            }

//            log.info("Presence updated: {} -> {}", username, online);

        } catch (Exception e) {
//            log.error("Presence sync error", e);
        }
    }
}