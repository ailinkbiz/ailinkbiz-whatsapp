//package com.br.ailinkbiz.store;
//
//import com.br.ailinkbiz.model.ConversationState;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//
//import java.time.Instant;
//import java.util.Optional;
//
//@Component
//public class RedisConversationStore {
//
//    private static final String KEY_PREFIX = "conversation:whatsapp:";
//
//    private final RedisTemplate<String, Object> redisTemplate;
//
//    public RedisConversationStore(RedisTemplate<String, Object> redisTemplate) {
//        this.redisTemplate = redisTemplate;
//    }
//
//    /**
//     * Salva ou atualiza a conversa inteira em UMA única chave
//     */
//    public void saveConversation(String phone, ConversationState state) {
//        String key = buildKey(phone);
//
//        redisTemplate.opsForHash().put(key, "state", state.name());
//        redisTemplate.opsForHash().put(
//                key,
//                "lastInteraction",
//                String.valueOf(Instant.now().getEpochSecond())
//        );
//    }
//
//    /**
//     * Recupera o estado da conversa
//     */
//    public Optional<ConversationState> getState(String phone) {
//        String key = buildKey(phone);
//
//        Object value = redisTemplate.opsForHash().get(key, "state");
//        if (value == null) {
//            return Optional.empty();
//        }
//
//        return Optional.of(ConversationState.valueOf(value.toString()));
//    }
//
//    /**
//     * Remove completamente a conversa do Redis
//     */
//    public void clearConversation(String phone) {
//        redisTemplate.delete(buildKey(phone));
//    }
//
//    private String buildKey(String phone) {
//        return KEY_PREFIX + phone;
//    }
//
//}
//
////@Component
////public class RedisConversationStore {
////
////    private static final String STATE_KEY = "conversation:state:";
////    private static final String LAST_INTERACTION_KEY = "conversation:lastInteraction:";
////
////    private final StringRedisTemplate redis;
////
////    public RedisConversationStore(StringRedisTemplate redis) {
////        this.redis = redis;
////    }
////
////    public ConversationState getState(String user) {
////        String value = redis.opsForValue().get(STATE_KEY + user);
////        return value == null ? ConversationState.NEW : ConversationState.valueOf(value);
////    }
////
////    public void setState(String user, ConversationState state) {
////        redis.opsForValue().set(STATE_KEY + user, state.name());
////        touch(user);
////    }
////
////    public void touch(String user) {
////        redis.opsForValue().set(LAST_INTERACTION_KEY + user, Instant.now().toString());
////    }
////
////    public Instant getLastInteraction(String user) {
////        String value = redis.opsForValue().get(LAST_INTERACTION_KEY + user);
////        return value == null ? null : Instant.parse(value);
////    }
////
////    public Map<String, ConversationState> getAllStates() {
////        return redis.keys(STATE_KEY + "*").stream()
////                .collect(Collectors.toMap(
////                        k -> k.replace(STATE_KEY, ""),
////                        k -> ConversationState.valueOf(redis.opsForValue().get(k))
////                ));
////    }
////
////    public void clear(String user) {
////        redis.delete(STATE_KEY + user);
////        redis.delete(LAST_INTERACTION_KEY + user);
////    }
////
////
////}
