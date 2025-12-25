package com.br.ailinkbiz.service;

import com.br.ailinkbiz.dto.UserConversationOverviewDTO;
import com.br.ailinkbiz.model.ConversationLog;
import com.br.ailinkbiz.store.ConversationLogStore;
import com.br.ailinkbiz.store.ConversationStore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class UserConversationOverviewService {

    private final ConversationStore conversationStore;

    public UserConversationOverviewService(ConversationStore conversationStore) {
        this.conversationStore = conversationStore;
    }

    /**
     * Overview de usuários POR CLIENTE
     */
    public List<UserConversationOverviewDTO> getOverview(String clientId) {

        // 🔑 Fonte da verdade: conversas do cliente
        Set<String> conversationIds =
                conversationStore.getClientConversationIds(clientId);

        if (conversationIds.isEmpty()) {
            return List.of();
        }

        List<ConversationLog> allLogs = ConversationLogStore.getAll();

        // Mapa: userId -> última conversa desse usuário
        Map<String, ConversationLog> lastLogByUser = new HashMap<>();

        for (String conversationId : conversationIds) {

            allLogs.stream()
                    .filter(log -> conversationId.equals(log.getConversationId()))
                    .max(Comparator.comparing(ConversationLog::getTimestamp))
                    .ifPresent(lastLog -> {

                        String userId = lastLog.getUserId();

                        ConversationLog existing = lastLogByUser.get(userId);
                        if (existing == null ||
                                lastLog.getTimestamp().isAfter(existing.getTimestamp())) {
                            lastLogByUser.put(userId, lastLog);
                        }
                    });
        }

        List<UserConversationOverviewDTO> result = new ArrayList<>();

        for (var entry : lastLogByUser.entrySet()) {

            String userId = entry.getKey();
            ConversationLog lastLog = entry.getValue();
            String conversationId = lastLog.getConversationId();

            Map<Object, Object> data =
                    conversationStore.getConversationData(conversationId);

            String rawStatus =
                    (String) data.getOrDefault("status", "UNKNOWN");

            String status = mapStatus(rawStatus);

            LocalDateTime lastActivity =
                    lastLog.getTimestamp()
                            .atZone(ZoneId.of("America/Sao_Paulo"))
                            .toLocalDateTime();

            result.add(new UserConversationOverviewDTO(
                    maskUser(userId),
                    status,
                    lastActivity
            ));
        }

        result.sort(
                Comparator.comparing(UserConversationOverviewDTO::lastActivity)
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

    private String maskUser(String userId) {
        if (userId == null || userId.length() < 4) return "Usuário";
        return userId.substring(0, 3) + " •••• " +
                userId.substring(userId.length() - 4);
    }
}
