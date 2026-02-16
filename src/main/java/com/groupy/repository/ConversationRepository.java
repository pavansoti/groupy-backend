package com.groupy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.groupy.entity.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("""
        SELECT c FROM Conversation c
        JOIN c.participants p1
        JOIN c.participants p2
        WHERE p1.id = :user1 AND p2.id = :user2
    """)
    Optional<Conversation> findConversationBetween(
            @Param("user1") Long user1,
            @Param("user2") Long user2
    );

    @Query("""
        SELECT c FROM Conversation c
        JOIN c.participants p
        WHERE p.id = :userId
        ORDER BY c.lastMessageTime DESC
    """)
    List<Conversation> findByUserId(@Param("userId") Long userId);
}
