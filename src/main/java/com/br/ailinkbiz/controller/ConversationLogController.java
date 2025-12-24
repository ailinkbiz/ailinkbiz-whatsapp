package com.br.ailinkbiz.controller;

import com.br.ailinkbiz.dto.HandoffConversationDTO;
import com.br.ailinkbiz.logging.ConversationLogger;
import com.br.ailinkbiz.model.ConversationLog;
import com.br.ailinkbiz.model.ConversationState;
import com.br.ailinkbiz.model.DecisionSource;
import com.br.ailinkbiz.store.ConversationLogStore;
import com.br.ailinkbiz.store.ConversationStore;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/logs")
public class ConversationLogController {

    private final ConversationStore conversationStore;
    private final ConversationLogger conversationLogger;

    public ConversationLogController(
            ConversationStore conversationStore,
            ConversationLogger conversationLogger
    ) {
        this.conversationStore = conversationStore;
        this.conversationLogger = conversationLogger;
    }

    /**
     * Lista logs (opcionalmente filtrando por estado)
     */
    @GetMapping(produces = "application/json")
    public List<ConversationLog> getLogs() {
        return ConversationLogStore.getAll();
    }

    /**
     * Lista logs de um usuário específico
     */
    @GetMapping(value = "/user/{user}", produces = "application/json")
    public List<ConversationLog> getLogsByUser(@PathVariable String user) {

        String normalizedUser = normalize(user);

        return ConversationLogStore.getAll().stream()
                .filter(log -> log.getFrom().equals(normalizedUser))
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

                    List<ConversationLog> logs = ConversationLogStore.getAll().stream()
                            .filter(log -> log.getFrom().equals(user))
                            .toList();

                    LocalDateTime lastMessageAt =
                            conversationStore.getLastInteraction(user)
                                    .map(instant ->
                                            instant.atZone(ZoneId.of("America/Sao_Paulo"))
                                                    .toLocalDateTime()
                                    )
                                    .orElse(null);

                    if (logs.isEmpty()) {
                        return new HandoffConversationDTO(
                                user,
                                "(histórico indisponível após reinício)",
                                lastMessageAt
                        );
                    }

                    Optional<ConversationLog> lastUserInput =
                            logs.stream()
                                    .filter(log ->
                                            log.getInput() != null &&
                                                    !log.getInput().isBlank()
                                    )
                                    .reduce((a, b) -> b);

                    ConversationLog last =
                            lastUserInput.orElse(logs.get(logs.size() - 1));

                    return new HandoffConversationDTO(
                            user,
                            last.getInput(),
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

        conversationLogger.logTurn(
                "DEFAULT",
                normalized,
                "DEFAULT",
                ConversationState.HUMAN_HANDOFF.name(),
                "SYSTEM_CLOSE_HANDOFF",
                "Atendimento humano encerrado",
                DecisionSource.SYSTEM
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
