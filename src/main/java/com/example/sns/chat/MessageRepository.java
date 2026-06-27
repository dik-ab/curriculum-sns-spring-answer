package com.example.sns.chat;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationOrderByCreatedAtAsc(Conversation conversation);
    Optional<Message> findFirstByConversationOrderByCreatedAtDesc(Conversation conversation);
}

