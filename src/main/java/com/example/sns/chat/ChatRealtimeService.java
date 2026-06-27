package com.example.sns.chat;

import com.example.sns.auth.AuthService;
import com.example.sns.auth.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatRealtimeService {
    private final AuthService auth;
    private final ConversationRepository conversations;
    private final MessageRepository messages;

    public ChatRealtimeService(AuthService auth, ConversationRepository conversations, MessageRepository messages) {
        this.auth = auth;
        this.conversations = conversations;
        this.messages = messages;
    }

    @Transactional(readOnly = true)
    public User authenticate(String token) {
        return auth.currentUserByToken(token);
    }

    @Transactional(readOnly = true)
    public boolean isParticipant(Long userId, Long conversationId) {
        return conversations.findById(conversationId)
            .map(conversation -> conversation.getUserOne().getId().equals(userId) || conversation.getUserTwo().getId().equals(userId))
            .orElse(false);
    }

    @Transactional
    public ChatController.MessageDto createMessage(Long userId, Long conversationId, String content) {
        Conversation conversation = conversations.findById(conversationId)
            .filter(item -> item.getUserOne().getId().equals(userId) || item.getUserTwo().getId().equals(userId))
            .orElseThrow();
        User sender = conversation.getUserOne().getId().equals(userId) ? conversation.getUserOne() : conversation.getUserTwo();
        return ChatController.MessageDto.from(messages.save(new Message(conversation, sender, content)));
    }
}
