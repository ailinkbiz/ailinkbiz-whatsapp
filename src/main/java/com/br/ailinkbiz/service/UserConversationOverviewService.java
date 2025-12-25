package com.br.ailinkbiz.service;

import com.br.ailinkbiz.dto.UserConversationOverviewDTO;
import com.br.ailinkbiz.model.ConversationLog;
import com.br.ailinkbiz.model.ConversationState;
import com.br.ailinkbiz.store.ConversationLogStore;
import com.br.ailinkbiz.store.ConversationStore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserConversationOverviewService {

    private final ConversationStore conversationStore;

    public UserConversationOverviewService(ConversationStore conversationStore) {
        this.conversationStore = conversationStore;
    }

    public List<UserConversationOverviewDTO> getOverview() {

        Map<String, List<ConversationLog>> logsByUser =
                ConversationLogStore.getAll().stream()
                        .collect(Collectors.groupingBy(ConversationLog::getUserId));

        List<UserConversationOverviewDTO> result = new ArrayList<>();

        for (var entry : logsByUser.entrySet()) {

            String userId = entry.getKey();
            List<ConversationLog> logs = entry.getValue();

            logs.sort(Comparator.comparing(ConversationLog::getTimestamp));
            ConversationLog lastLog = logs.get(logs.size() - 1);

            Optional<ConversationState> state =
                    conversationStore.getState(userId);

            String status;

            if (state.isPresent()) {
                status = state.get() == ConversationState.HUMAN_HANDOFF
                        ? "EM_ATENDIMENTO"
                        : "ATIVA";
            } else {
                status = "ENCERRADA";
            }

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

        return result;
    }

    private String maskUser(String userId) {
        if (userId == null || userId.length() < 4) return "Usuário";
        return userId.substring(0, 3) + " •••• " +
                userId.substring(userId.length() - 4);
    }

}