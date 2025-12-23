package com.br.ailinkbiz.store;

import com.br.ailinkbiz.model.ConversationState;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ConversationStore {

    private static final String KEY_PREFIX = "conversation:whatsapp:";

    private final StringRedisTemplate redis;

    public ConversationStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

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

    public Optional<Instant> getLastInteraction(String phone) {
        Object value = redis.opsForHash().get(buildKey(phone), "lastInteraction");
        return value == null
                ? Optional.empty()
                : Optional.of(Instant.parse(value.toString()));
    }

    public void clearConversation(String phone) {
        redis.delete(buildKey(phone));
    }

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

}

