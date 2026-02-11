package com.groupy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.groupy.dto.NotificationDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(Long userId, NotificationDto notification) {
        log.info("Sending notification to user {}: {}", userId, notification);
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, notification);
    }
}
