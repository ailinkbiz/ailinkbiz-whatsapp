package com.br.ailinkbiz.controller;

import com.br.ailinkbiz.dto.HandoffConversationDTO;
import com.br.ailinkbiz.model.ConversationLog;
import com.br.ailinkbiz.model.ConversationState;
import com.br.ailinkbiz.store.ConversationLogStore;
import com.br.ailinkbiz.store.ConversationStore;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.br.ailinkbiz.Util.Utils.addLog;

@RestController
@RequestMapping("/logs")
public class ConversationLogController {

    private final ConversationStore conversationStore;

    public ConversationLogController(ConversationStore conversationStore) {
        this.conversationStore = conversationStore;
    }

    /**
     * Lista logs (opcionalmente filtrando por estado)
     */
    @GetMapping(produces = "application/json")
    public List<ConversationLog> getLogs(
            @RequestParam(required = false) ConversationState state
    ) {
        if (state == null) {
            return ConversationLogStore.getAll();
        }

        return ConversationLogStore.getAll().stream()
                .filter(log -> log.getState() == state)
                .toList();
    }

    /**
     * Lista logs de um usuário específico
     */
    @GetMapping(value = "/user/{user}", produces = "application/json")
    public List<ConversationLog> getLogsByUser(@PathVariable String user) {

        String normalizedUser = normalize(user);

        return ConversationLogStore.getAll().stream()
                .filter(log -> log.getUser().equals(normalizedUser))
                .toList();
    }

    /**
     * LISTA DE HANDOFF
     * Fonte da verdade: Redis (ConversationStore)
     * Logs são apenas complemento visual
     */
    @GetMapping(value = "/handoff", produces = "application/json")
    public List<HandoffConversationDTO> getHandoffConversations() {

        return conversationStore.getAllStates().entrySet().stream()

                // somente usuários em HANDOFF segundo o Redis
                .filter(entry -> entry.getValue() == ConversationState.HUMAN_HANDOFF)

                .map(entry -> {

                    String user = entry.getKey();

                    // logs podem existir ou não (ex: após restart)
                    List<ConversationLog> logs = ConversationLogStore.getAll().stream()
                            .filter(log -> log.getUser().equals(user))
                            .toList();

                    // lastInteraction SEMPRE vem do Redis
                    LocalDateTime lastMessageAt =
                            conversationStore.getLastInteraction(user)
                                    .map(instant ->
                                            instant.atZone(ZoneId.of("America/Sao_Paulo"))
                                                    .toLocalDateTime()
                                    )
                                    .orElse(null);

                    // sem histórico disponível
                    if (logs.isEmpty()) {
                        return new HandoffConversationDTO(
                                user,
                                "(histórico indisponível após reinício)",
                                lastMessageAt
                        );
                    }

                    // tenta última mensagem INBOUND
                    Optional<ConversationLog> lastInbound =
                            logs.stream()
                                    .filter(l -> l.getDirection() == ConversationLog.Direction.INBOUND)
                                    .reduce((a, b) -> b);

                    ConversationLog last =
                            lastInbound.orElse(logs.get(logs.size() - 1));

                    return new HandoffConversationDTO(
                            user,
                            last.getMessage(),
                            last.getTimestamp()
                    );
                })
                .toList();
    }

    /**
     * Encerra handoff manualmente
     */
    @PostMapping(value = "/handoff/{user}/close", produces = "application/json")
    public Map<String, Object> closeHandoff(@PathVariable String user) {

        String normalized = normalize(user);

        ConversationState currentState = conversationStore
                .getState(normalized)
                .orElse(ConversationState.NEW);

        if (currentState != ConversationState.HUMAN_HANDOFF) {
            return Map.of(
                    "closed", false,
                    "reason", "User is not in handoff"
            );
        }

        conversationStore.saveConversation(normalized, ConversationState.NEW);

        addLog(
                normalized,
                "Atendimento humano encerrado",
                ConversationState.NEW,
                ConversationLog.Direction.SYSTEM
        );

        return Map.of(
                "closed", true,
                "user", normalized
        );
    }

    /**
     * Normaliza telefone para o padrão interno
     */
    private String normalize(String user) {

        user = user.replace("whatsapp:", "").replace(" ", "");

        if (!user.startsWith("+")) {
            user = "+" + user;
        }

        return "whatsapp:" + user;
    }
}

//package com.br.ailinkbiz.controller;
//
//import com.br.ailinkbiz.dto.HandoffConversationDTO;
//import com.br.ailinkbiz.model.ConversationLog;
//import com.br.ailinkbiz.model.ConversationState;
//import com.br.ailinkbiz.store.ConversationLogStore;
//import com.br.ailinkbiz.store.ConversationStore;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//import static com.br.ailinkbiz.Util.Utils.addLog;
//
//@RestController
//@RequestMapping("/logs")
//public class ConversationLogController {
//
//    private final ConversationStore conversationStore;
//
//    public ConversationLogController(ConversationStore conversationStore) {
//        this.conversationStore = conversationStore;
//    }
//
//    @GetMapping(produces = "application/json")
//    public List<ConversationLog> getLogs(
//            @RequestParam(required = false) ConversationState state
//    ) {
//        if (state == null) {
//            return ConversationLogStore.getAll();
//        }
//
//        return ConversationLogStore.getAll().stream()
//                .filter(log -> log.getState() == state)
//                .toList();
//    }
//
//    @GetMapping(value = "/user/{user}", produces = "application/json")
//    public List<ConversationLog> getLogsByUser(@PathVariable String user) {
//
//        String normalizedUser = normalize(user);
//
//        return ConversationLogStore.getAll().stream()
//                .filter(log -> log.getUser().equals(normalizedUser))
//                .toList();
//    }
//
//    @GetMapping(value = "/handoff", produces = "application/json")
//    public List<HandoffConversationDTO> getHandoffConversations() {
//
//        return ConversationLogStore.getAll().stream()
//                .map(ConversationLog::getUser)
//                .distinct()
//                .filter(user ->
//                        conversationStore
//                                .getState(user)
//                                .orElse(ConversationState.NEW)
//                                == ConversationState.HUMAN_HANDOFF
//                )
//                .map(user -> {
//
//                    var logs = ConversationLogStore.getAll().stream()
//                            .filter(log -> log.getUser().equals(user))
//                            .toList();
//
//                    if (logs.isEmpty()) {
//
//                        Optional<Instant> lastInteraction =
//                                conversationStore.getLastInteraction(user);
//
//                        LocalDateTime lastMessageAt =
//                                lastInteraction
//                                        .map(instant ->
//                                                instant.atZone(ZoneId.systemDefault())
//                                                        .toLocalDateTime()
//                                        )
//                                        .orElse(null);
//
//                        return new HandoffConversationDTO(
//                                user,
//                                "(sem mensagens registradas)",
//                                lastMessageAt
//                        );
//                    }
//
//                    var lastInbound = logs.stream()
//                            .filter(l -> l.getDirection() == ConversationLog.Direction.INBOUND)
//                            .reduce((a, b) -> b);
//
//                    var last = lastInbound.orElse(logs.get(logs.size() - 1));
//
//                    return new HandoffConversationDTO(
//                            user,
//                            last.getMessage(),
//                            last.getTimestamp()
//                    );
//                })
//                .toList();
//    }
//
//    @PostMapping(value = "/handoff/{user}/close", produces = "application/json")
//    public Map<String, Object> closeHandoff(@PathVariable String user) {
//
//        String normalized = normalize(user);
//
//        ConversationState currentState = conversationStore
//                .getState(normalized)
//                .orElse(ConversationState.NEW);
//
//        if (currentState != ConversationState.HUMAN_HANDOFF) {
//            return Map.of(
//                    "closed", false,
//                    "reason", "User is not in handoff"
//            );
//        }
//
//        conversationStore.saveConversation(normalized, ConversationState.NEW);
//
//        addLog(
//                normalized,
//                "Atendimento humano encerrado",
//                ConversationState.NEW,
//                ConversationLog.Direction.SYSTEM
//        );
//
//        return Map.of(
//                "closed", true,
//                "user", normalized
//        );
//    }
//
//    private String normalize(String user) {
//
//        user = user.replace("whatsapp:", "").replace(" ", "");
//
//        if (!user.startsWith("+")) {
//            user = "+" + user;
//        }
//
//        return "whatsapp:" + user;
//    }
//
//}
