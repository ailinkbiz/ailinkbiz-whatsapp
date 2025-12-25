package com.br.ailinkbiz.controller;

import com.br.ailinkbiz.dto.HandoffConversationDTO;
import com.br.ailinkbiz.logging.ConversationLogger;
import com.br.ailinkbiz.model.ConversationLog;
import com.br.ailinkbiz.model.DecisionSource;
import com.br.ailinkbiz.persistence.entity.ConversationClosure;
import com.br.ailinkbiz.repository.ConversationClosureRepository;
import com.br.ailinkbiz.store.ConversationLogStore;
import com.br.ailinkbiz.store.ConversationStore;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/logs")
public class ConversationLogController {

    private final ConversationStore conversationStore;
    private final ConversationLogger conversationLogger;
    private final ConversationClosureRepository closureRepository;

    public ConversationLogController(
            ConversationStore conversationStore,
            ConversationLogger conversationLogger,
            ConversationClosureRepository closureRepository
    ) {
        this.conversationStore = conversationStore;
        this.conversationLogger = conversationLogger;
        this.closureRepository = closureRepository;
    }

    /**
     * Lista TODOS os logs (debug / auditoria)
     */
    @GetMapping(produces = "application/json")
    public List<ConversationLog> getLogs() {
        return ConversationLogStore.getAll();
    }

    /**
     * Lista logs de UMA conversa
     */
    @GetMapping(value = "/conversation/{conversationId}", produces = "application/json")
    public List<ConversationLog> getLogsByConversation(
            @PathVariable String conversationId
    ) {
        return ConversationLogStore.getAll().stream()
                .filter(log -> conversationId.equals(log.getConversationId()))
                .toList();
    }

    /**
     * LISTA DE HANDOFF
     * Fonte da verdade: Redis (status = HANDOFF)
     */
    @GetMapping(value = "/handoff", produces = "application/json")
    public List<HandoffConversationDTO> getHandoffConversations(
            @RequestParam String clientId
    ) {

        Set<String> handoffConversationIds =
                conversationStore.getHandoffConversationIds(clientId);

        if (handoffConversationIds.isEmpty()) {
            return List.of();
        }

        List<ConversationLog> allLogs = ConversationLogStore.getAll();

        return handoffConversationIds.stream()
                .map(conversationId -> {

                    Map<Object, Object> data =
                            conversationStore.getConversationData(conversationId);

                    // proteção contra inconsistência
                    if (!"HANDOFF".equals(data.get("status"))) {
                        return null;
                    }

                    List<ConversationLog> logs =
                            allLogs.stream()
                                    .filter(log ->
                                            conversationId.equals(log.getConversationId())
                                    )
                                    .sorted(Comparator.comparing(ConversationLog::getTimestamp))
                                    .toList();

                    String lastUserInput = "(sem mensagem)";
                    LocalDateTime lastActivity = null;

                    if (!logs.isEmpty()) {

                        ConversationLog last =
                                logs.stream()
                                        .filter(log ->
                                                log.getInput() != null &&
                                                        !log.getInput().isBlank()
                                        )
                                        .reduce((a, b) -> b)
                                        .orElse(logs.get(logs.size() - 1));

                        lastUserInput = last.getInput();
                        lastActivity =
                                last.getTimestamp()
                                        .atZone(ZoneId.of("America/Sao_Paulo"))
                                        .toLocalDateTime();
                    }

                    return new HandoffConversationDTO(
                            conversationId,
                            lastUserInput,
                            lastActivity
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Encerra manualmente um handoff
     */
    @PostMapping(value = "/handoff/{conversationId}/close", produces = "application/json")
    public Map<String, Object> closeHandoff(
            @PathVariable String conversationId,
            @RequestParam String clientId
    ) {

        // 🔑 dados da conversa (fonte da verdade)
        Map<Object, Object> data =
                conversationStore.getConversationData(conversationId);

        String user =
                data.get("phone") != null
                        ? data.get("phone").toString()
                        : "UNKNOWN";

        // 🔒 auditoria em banco
        closureRepository.save(
                new ConversationClosure(
                        UUID.fromString(conversationId),
                        clientId,
                        user,
                        "MANUAL_HANDOFF_CLOSE"
                )
        );

        // 🔑 fechamento canônico no domínio
        conversationStore.closeConversation(
                conversationId,
                "MANUAL_HANDOFF_CLOSE"
        );

        // 🧾 log de sistema
        conversationLogger.logTurn(
                conversationId,
                clientId,
                user,
                "DEFAULT",
                "HANDOFF",
                "SYSTEM_CLOSE_HANDOFF",
                "Atendimento humano encerrado manualmente",
                DecisionSource.SYSTEM
        );

        return Map.of(
                "closed", true,
                "conversationId", conversationId
        );
    }
}
