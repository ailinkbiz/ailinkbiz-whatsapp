package com.br.ailinkbiz.store;

import com.br.ailinkbiz.model.ConversationState;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class ConversationStore {

    private static final String CONVERSATION_KEY = "conversation:";
    private static final String CLIENT_CONVERSATIONS = "client:%s:conversations";
    private static final String CLIENT_CONVERSATIONS_ACTIVE = "client:%s:conversations:ACTIVE";
    private static final String CLIENT_CONVERSATIONS_CLOSED = "client:%s:conversations:CLOSED";
    private static final String CLIENT_CONVERSATIONS_HANDOFF = "client:%s:conversations:HANDOFF";
    private static final String ACTIVE_CONVERSATION_KEY = "activeConversation:%s:%s";

    private final StringRedisTemplate redis;

    public ConversationStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /* =========================
       CREATE
       ========================= */

    public String createConversation(String phone, String clientId) {

        String conversationId = UUID.randomUUID().toString();
        String key = CONVERSATION_KEY + conversationId;
        Instant now = Instant.now();

        redis.opsForHash().put(key, "conversationId", conversationId);
        redis.opsForHash().put(key, "phone", phone);
        redis.opsForHash().put(key, "clientId", clientId);
        redis.opsForHash().put(key, "state", ConversationState.NEW.name());
        redis.opsForHash().put(key, "status", "ACTIVE");
        redis.opsForHash().put(key, "startedAt", now.toString());
        redis.opsForHash().put(key, "lastInteraction", now.toString());

        redis.opsForSet().add(
                String.format(CLIENT_CONVERSATIONS, clientId),
                conversationId
        );

        redis.opsForSet().add(
                String.format(CLIENT_CONVERSATIONS_ACTIVE, clientId),
                conversationId
        );

        return conversationId;
    }

    /* =========================
       STATE
       ========================= */

    public void updateState(String conversationId, ConversationState state) {

        String key = CONVERSATION_KEY + conversationId;

        redis.opsForHash().put(key, "state", state.name());
        redis.opsForHash().put(key, "lastInteraction", Instant.now().toString());
    }

    /* =========================
       HANDOFF
       ========================= */

    public void markHandoff(String conversationId) {

        String key = CONVERSATION_KEY + conversationId;
        String clientId = (String) redis.opsForHash().get(key, "clientId");

        if (clientId == null) return;

        redis.opsForHash().put(key, "status", "HANDOFF");
        redis.opsForHash().put(key, "lastInteraction", Instant.now().toString());

        redis.opsForSet().remove(
                String.format(CLIENT_CONVERSATIONS_ACTIVE, clientId),
                conversationId
        );

        redis.opsForSet().add(
                String.format(CLIENT_CONVERSATIONS_HANDOFF, clientId),
                conversationId
        );
    }

    /* =========================
       CLOSE
       ========================= */

    public void closeConversation(String conversationId, String closeReason) {

        String key = CONVERSATION_KEY + conversationId;
        String clientId = (String) redis.opsForHash().get(key, "clientId");

        Object status = redis.opsForHash().get(key, "status");
        if (!"ACTIVE".equals(status) && !"HANDOFF".equals(status)) {
            return;
        }

        redis.opsForHash().put(key, "status", "CLOSED");
        redis.opsForHash().put(key, "state", ConversationState.CLOSED.name());
        redis.opsForHash().put(key, "closedAt", Instant.now().toString());
        redis.opsForHash().put(key, "closeReason", closeReason);

        redis.opsForSet().remove(
                String.format(CLIENT_CONVERSATIONS_ACTIVE, clientId),
                conversationId
        );

        redis.opsForSet().remove(
                String.format(CLIENT_CONVERSATIONS_HANDOFF, clientId),
                conversationId
        );

        redis.opsForSet().add(
                String.format(CLIENT_CONVERSATIONS_CLOSED, clientId),
                conversationId
        );
    }

    /* =========================
       READ
       ========================= */

    public Map<Object, Object> getConversationData(String conversationId) {
        return redis.opsForHash().entries(CONVERSATION_KEY + conversationId);
    }

    public Set<String> getHandoffConversationIds(String clientId) {
        return redis.opsForSet().members(
                String.format(CLIENT_CONVERSATIONS_HANDOFF, clientId)
        );
    }

    public Optional<ConversationState> getStateByConversation(String conversationId) {
        Object v = redis.opsForHash().get(CONVERSATION_KEY + conversationId, "state");
        return v == null ? Optional.empty() : Optional.of(ConversationState.valueOf(v.toString()));
    }

    /* =========================
       ACTIVE CONVERSATION
       ========================= */

    public Optional<String> getActiveConversationId(String clientId, String userId) {
        return Optional.ofNullable(
                redis.opsForValue().get(
                        String.format(ACTIVE_CONVERSATION_KEY, clientId, userId)
                )
        );
    }

    public void setActiveConversation(String clientId, String userId, String conversationId) {
        redis.opsForValue().set(
                String.format(ACTIVE_CONVERSATION_KEY, clientId, userId),
                conversationId
        );
    }

    public void clearActiveConversation(String clientId, String userId) {
        redis.delete(
                String.format(ACTIVE_CONVERSATION_KEY, clientId, userId)
        );
    }

    public Set<String> getAllHandoffConversationIds() {
        return redis.keys("client:*:conversations:HANDOFF")
                .stream()
                .flatMap(key -> redis.opsForSet().members(key).stream())
                .collect(java.util.stream.Collectors.toSet());
    }

    public Set<String> getClientConversationIds(String clientId) {
        return redis.opsForSet().members(
                String.format("client:%s:conversations", clientId)
        );
    }


}
