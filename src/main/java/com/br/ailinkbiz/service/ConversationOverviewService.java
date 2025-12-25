package com.br.ailinkbiz.service;

import com.br.ailinkbiz.dto.ConversationOverviewDTO;
import com.br.ailinkbiz.model.ConversationLog;
import com.br.ailinkbiz.model.ConversationState;
import com.br.ailinkbiz.model.DecisionSource;
import com.br.ailinkbiz.store.ConversationLogStore;
import com.br.ailinkbiz.store.ConversationStore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConversationOverviewService {

    private final ConversationStore conversationStore;

    public ConversationOverviewService(ConversationStore conversationStore) {
        this.conversationStore = conversationStore;
    }

    public List<ConversationOverviewDTO> getOverview() {

        Map<String, List<ConversationLog>> logsByConversation =
                ConversationLogStore.getAll().stream()
                        .collect(Collectors.groupingBy(ConversationLog::getConversationId));

        List<ConversationOverviewDTO> result = new ArrayList<>();

        for (var entry : logsByConversation.entrySet()) {

            String conversationId = entry.getKey();
            List<ConversationLog> logs = entry.getValue();

            logs.sort(Comparator.comparing(ConversationLog::getTimestamp));

            ConversationLog lastLog = logs.get(logs.size() - 1);

            String userId = lastLog.getUserId();
            String userMasked = maskUser(userId);

            // conversa ativa atual do usuário (se existir)
            Optional<String> activeConversationId =
                    conversationStore.getConversationId(userId);

            boolean isActiveConversation =
                    activeConversationId.isPresent()
                            && activeConversationId.get().equals(conversationId);

            String status;
            String closeReason = null;

            if (isActiveConversation) {

                ConversationState state =
                        conversationStore.getState(userId).orElse(null);

                if (state == ConversationState.HUMAN_HANDOFF) {
                    status = "EM_ATENDIMENTO";
                } else {
                    status = "ATIVA";
                }

            } else {
                status = "ENCERRADA";
                closeReason = resolveCloseReason(logs);
            }

            LocalDateTime lastActivity =
                    lastLog.getTimestamp()
                            .atZone(ZoneId.of("America/Sao_Paulo"))
                            .toLocalDateTime();

            result.add(new ConversationOverviewDTO(
                    conversationId,
                    userMasked,
                    status,
                    closeReason,
                    lastActivity
            ));
        }

        return result;
    }

    private String resolveCloseReason(List<ConversationLog> logs) {

        return logs.stream()
                .filter(log -> log.getDecisionSource() == DecisionSource.SYSTEM)
                .reduce((a, b) -> b)
                .map(log -> switch (log.getInput()) {
                    case "SYSTEM_TIMEOUT" -> "Encerrada por inatividade";
                    default -> "Encerrada pelo sistema";
                })
                .orElse("Atendimento concluído");
    }

    private String maskUser(String userId) {
        if (userId == null || userId.length() < 4) return "Usuário";
        return userId.substring(0, 3) + " •••• " +
                userId.substring(userId.length() - 4);
    }

}
