package com.example.sns.chat;

import com.corundumstudio.socketio.AuthTokenResult;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.example.sns.auth.User;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "socketio.enabled", havingValue = "true", matchIfMissing = true)
public class SocketIoChatServer {
    private final ChatRealtimeService chat;
    private final String frontendUrl;
    private final int port;
    private SocketIOServer server;

    public SocketIoChatServer(
        ChatRealtimeService chat,
        @Value("${app.frontend-url}") String frontendUrl,
        @Value("${socketio.port}") int port
    ) {
        this.chat = chat;
        this.frontendUrl = frontendUrl;
        this.port = port;
    }

    @PostConstruct
    public void start() {
        Configuration config = new Configuration();
        config.setPort(port);
        config.setOrigin(frontendUrl);

        server = new SocketIOServer(config);
        SocketIONamespace namespace = server.addNamespace("/chat");

        namespace.addAuthTokenListener((authToken, client) -> {
            String token = extractToken(authToken);
            if (token == null || token.isBlank()) {
                return new AuthTokenResult(false, "認証トークンがありません");
            }
            try {
                User user = chat.authenticate(token);
                client.set("userId", user.getId());
                return AuthTokenResult.AuthTokenResultSuccess;
            } catch (RuntimeException error) {
                return new AuthTokenResult(false, "認証トークンが無効です");
            }
        });

        namespace.addEventListener("joinConversation", JoinConversationPayload.class, (client, payload, ack) -> {
            Long userId = client.get("userId");
            if (userId == null || payload.conversationId() == null) return;
            if (chat.isParticipant(userId, payload.conversationId())) {
                client.joinRoom(room(payload.conversationId()));
            }
        });

        namespace.addEventListener("sendMessage", SendMessagePayload.class, (client, payload, ack) -> {
            Long userId = client.get("userId");
            if (userId == null || payload.conversationId() == null || payload.content() == null) return;
            String content = payload.content().trim();
            if (content.isBlank() || content.length() > 1000) return;
            if (!chat.isParticipant(userId, payload.conversationId())) return;
            ChatController.MessageDto message = chat.createMessage(userId, payload.conversationId(), content);
            namespace.getRoomOperations(room(payload.conversationId())).sendEvent("newMessage", message);
        });

        server.start();
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.stop();
        }
    }

    private static String extractToken(Object authToken) {
        if (authToken instanceof Map<?, ?> map) {
            Object token = map.get("token");
            return token instanceof String value ? value : null;
        }
        return authToken instanceof String value ? value : null;
    }

    private static String room(Long conversationId) {
        return "conversation:" + conversationId;
    }

    public record JoinConversationPayload(Long conversationId) {}
    public record SendMessagePayload(Long conversationId, String content) {}
}
