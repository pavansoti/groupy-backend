package com.groupy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.groupy.entity.Message;

import jakarta.transaction.Transactional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    int countByConversationIdAndSenderIdNotAndIsReadFalse(
            Long conversationId,
            Long userId
    );
    
    @Transactional
    @Modifying
    @Query("""
           UPDATE Message m
           SET m.isRead = true
           WHERE m.conversation.id = :conversationId
           AND m.sender.id <> :userId
           """)
    void markMessagesAsRead(Long conversationId, Long userId);

}
