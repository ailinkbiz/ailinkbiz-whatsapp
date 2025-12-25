package com.br.ailinkbiz.service;

import com.br.ailinkbiz.dto.ConversationOverviewDTO;
import com.br.ailinkbiz.model.ConversationLog;
import com.br.ailinkbiz.model.DecisionSource;
import com.br.ailinkbiz.store.ConversationLogStore;
import com.br.ailinkbiz.store.ConversationStore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class ConversationOverviewService {

    private final ConversationStore conversationStore;

    public ConversationOverviewService(ConversationStore conversationStore) {
        this.conversationStore = conversationStore;
    }

    /**
     * Overview de conversas POR CLIENTE
     */
    public List<ConversationOverviewDTO> getOverview(String clientId) {

        Set<String> conversationIds =
                conversationStore.getClientConversationIds(clientId);

        if (conversationIds.isEmpty()) {
            return List.of();
        }

        List<ConversationLog> allLogs = ConversationLogStore.getAll();
        List<ConversationOverviewDTO> result = new ArrayList<>();

        for (String conversationId : conversationIds) {

            List<ConversationLog> logs =
                    allLogs.stream()
                            .filter(log -> conversationId.equals(log.getConversationId()))
                            .sorted(Comparator.comparing(ConversationLog::getTimestamp))
                            .toList();

            if (logs.isEmpty()) {
                continue;
            }

            Map<Object, Object> data =
                    conversationStore.getConversationData(conversationId);

            String phone = (String) data.get("phone");
            String userMasked = maskUser(phone);

            String rawStatus =
                    (String) data.getOrDefault("status", "UNKNOWN");

            String status = mapStatus(rawStatus);

            String closeReason =
                    "CLOSED".equals(rawStatus)
                            ? resolveCloseReason(logs)
                            : null;

            ConversationLog lastLog = logs.get(logs.size() - 1);

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

        result.sort(
                Comparator.comparing(ConversationOverviewDTO::lastActivity)
                        .reversed()
        );

        return result;
    }

    /* =========================
       AUXILIARES
       ========================= */

    private String mapStatus(String status) {
        return switch (status) {
            case "ACTIVE" -> "ATIVA";
            case "HANDOFF" -> "EM_ATENDIMENTO";
            case "CLOSED" -> "ENCERRADA";
            default -> "DESCONHECIDA";
        };
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

    private String maskUser(String phone) {

        if (phone == null || phone.length() < 4) {
            return "Usuário";
        }

        // remove prefixo whatsapp: se existir
        phone = phone.replace("whatsapp:", "");

        return phone.substring(0, 3)
                + " •••• "
                + phone.substring(phone.length() - 4);
    }

}
