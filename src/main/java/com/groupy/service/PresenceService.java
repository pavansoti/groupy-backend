package com.groupy.service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class PresenceService {

    // userId -> number of active sessions
    private final Map<String, Integer> onlineUsers = new ConcurrentHashMap<>();

    public void userConnected(String username) {
        onlineUsers.merge(username, 1, Integer::sum);
    }

    public void userDisconnected(String username) {
        onlineUsers.computeIfPresent(username, (id, count) -> {
            if (count <= 1) return null;
            return count - 1;
        });
    }

    public boolean isOnline(String username) {
        return onlineUsers.containsKey(username);
    }

    public Set<String> getOnlineUsernames() {
        return onlineUsers.keySet();
    }
}
