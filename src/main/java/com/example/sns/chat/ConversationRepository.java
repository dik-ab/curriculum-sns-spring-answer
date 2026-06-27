package com.example.sns.chat;

import com.example.sns.auth.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByUserOneAndUserTwo(User userOne, User userTwo);
    List<Conversation> findByUserOneOrUserTwo(User userOne, User userTwo);
}

