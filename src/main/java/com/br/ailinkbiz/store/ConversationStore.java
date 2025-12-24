package com.br.ailinkbiz.store;

import com.br.ailinkbiz.model.ConversationState;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ConversationStore {

    private static final String KEY_PREFIX = "conversation:";

    private final StringRedisTemplate redis;

    public ConversationStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /* =========================
       STATE
       ========================= */

    public void saveConversation(String phone, ConversationState state) {
        String key = buildKey(phone);

        redis.opsForHash().put(key, "state", state.name());
        redis.opsForHash().put(
                key,
                "lastInteraction",
                Instant.now().toString()
        );
    }

    public Optional<ConversationState> getState(String phone) {
        Object value = redis.opsForHash().get(buildKey(phone), "state");
        return value == null
                ? Optional.empty()
                : Optional.of(ConversationState.valueOf(value.toString()));
    }

    /* =========================
       LAST INTERACTION
       ========================= */

    public Optional<Instant> getLastInteraction(String phone) {
        Object value = redis.opsForHash().get(buildKey(phone), "lastInteraction");
        return value == null
                ? Optional.empty()
                : Optional.of(Instant.parse(value.toString()));
    }

    /* =========================
       CONVERSATION ID
       ========================= */

    public Optional<String> getConversationId(String phone) {
        Object value = redis.opsForHash().get(buildKey(phone), "conversationId");
        return value == null
                ? Optional.empty()
                : Optional.of(value.toString());
    }

    public String getOrCreateConversationId(String phone) {

        return getConversationId(phone)
                .orElseGet(() -> {
                    String id = UUID.randomUUID().toString();
                    redis.opsForHash().put(
                            buildKey(phone),
                            "conversationId",
                            id
                    );
                    return id;
                });
    }

    /* =========================
       CLEAR
       ========================= */

    public void clearConversation(String phone) {
        redis.delete(buildKey(phone));
    }

    /* =========================
       UTILS
       ========================= */

    private String buildKey(String phone) {
        return KEY_PREFIX + phone;
    }

    public Map<String, ConversationState> getAllStates() {
        return redis.keys(KEY_PREFIX + "*").stream()
                .collect(Collectors.toMap(
                        key -> key.replace(KEY_PREFIX, ""),
                        key -> ConversationState.valueOf(
                                redis.opsForHash().get(key, "state").toString()
                        )
                ));
    }

    public Optional<String> getClientId(String phone) {
        Object value = redis.opsForHash().get(buildKey(phone), "clientId");
        return value == null
                ? Optional.empty()
                : Optional.of(value.toString());
    }

    public void saveClientId(String phone, String clientId) {
        redis.opsForHash().put(buildKey(phone), "clientId", clientId);
    }

}

