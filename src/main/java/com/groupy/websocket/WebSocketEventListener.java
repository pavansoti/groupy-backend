package com.groupy.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.groupy.dto.UserStatusResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    // supports multiple tabs
    private static final Map<String, Integer> onlineUsers = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnect(SessionConnectedEvent event) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(event.getMessage());

        if (accessor.getUser() == null) return;

        String username = accessor.getUser().getName();

        onlineUsers.merge(username, 1, Integer::sum);

        messagingTemplate.convertAndSend(
                "/topic/user-status",
                new UserStatusResponse(username, true)
        );
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(event.getMessage());

        if (accessor.getUser() == null) return;

        String username = accessor.getUser().getName();

        onlineUsers.computeIfPresent(username, (key, count) -> {
            if (count <= 1) {
                messagingTemplate.convertAndSend(
                        "/topic/user-status",
                        new UserStatusResponse(username, false)
                );
                return null;
            }
            return count - 1;
        });
    }
}