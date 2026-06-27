package com.example.sns.chat;

import com.example.sns.auth.*;
import com.example.sns.users.UserDto;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/conversations")
public class ChatController {
    private final AuthService auth;
    private final UserRepository users;
    private final ConversationRepository conversations;
    private final MessageRepository messages;

    public ChatController(AuthService auth, UserRepository users, ConversationRepository conversations, MessageRepository messages) {
        this.auth = auth;
        this.users = users;
        this.conversations = conversations;
        this.messages = messages;
    }

    @GetMapping
    public List<ConversationDto> index(@RequestHeader(value = "Authorization", required = false) String authorization, @CookieValue(value = "sns_session", required = false) String session) {
        User viewer = auth.currentUser(authorization, session);
        return conversations.findByUserOneOrUserTwo(viewer, viewer).stream()
            .map(conversation -> ConversationDto.from(conversation, viewer, messages))
            .sorted(Comparator.comparing((ConversationDto dto) -> dto.lastMessage() == null ? "" : dto.lastMessage().createdAt()).reversed())
            .toList();
    }

    @PostMapping
    public ConversationDto create(@RequestHeader(value = "Authorization", required = false) String authorization, @CookieValue(value = "sns_session", required = false) String session, @RequestBody CreateConversationRequest request) {
        User viewer = auth.currentUser(authorization, session);
        User partner = users.findByUsername(request.username())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーがありません"));
        if (viewer.getId().equals(partner.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "自分とは会話できません");
        }
        Conversation probe = new Conversation(viewer, partner);
        Conversation conversation = conversations.findByUserOneAndUserTwo(probe.getUserOne(), probe.getUserTwo())
            .orElseGet(() -> conversations.save(new Conversation(viewer, partner)));
        return ConversationDto.from(conversation, viewer, messages);
    }

    @GetMapping("/{id}/messages")
    public List<MessageDto> messages(@RequestHeader(value = "Authorization", required = false) String authorization, @CookieValue(value = "sns_session", required = false) String session, @PathVariable Long id) {
        User viewer = auth.currentUser(authorization, session);
        Conversation conversation = findConversationFor(viewer, id);
        return this.messages.findByConversationOrderByCreatedAtAsc(conversation).stream().map(MessageDto::from).toList();
    }

    @PostMapping("/{id}/messages")
    public MessageDto createMessage(@RequestHeader(value = "Authorization", required = false) String authorization, @CookieValue(value = "sns_session", required = false) String session, @PathVariable Long id, @RequestBody CreateMessageRequest request) {
        User viewer = auth.currentUser(authorization, session);
        Conversation conversation = findConversationFor(viewer, id);
        if (request.content() == null || request.content().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "メッセージを入力してください");
        }
        return MessageDto.from(messages.save(new Message(conversation, viewer, request.content().trim())));
    }

    private Conversation findConversationFor(User viewer, Long id) {
        Conversation conversation = conversations.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "会話がありません"));
        if (!conversation.includes(viewer)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "この会話は表示できません");
        }
        return conversation;
    }

    public record CreateConversationRequest(String username) {}
    public record CreateMessageRequest(String content) {}
    public record ConversationDto(Long id, UserDto partner, MessageDto lastMessage) {
        static ConversationDto from(Conversation conversation, User viewer, MessageRepository messages) {
            return new ConversationDto(
                conversation.getId(),
                UserDto.from(conversation.partnerOf(viewer)),
                messages.findFirstByConversationOrderByCreatedAtDesc(conversation).map(MessageDto::from).orElse(null)
            );
        }
    }
    public record MessageDto(Long id, Long conversationId, Long senderId, String content, String createdAt) {
        static MessageDto from(Message message) {
            return new MessageDto(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getContent(),
                message.getCreatedAt().toString()
            );
        }
    }
}
