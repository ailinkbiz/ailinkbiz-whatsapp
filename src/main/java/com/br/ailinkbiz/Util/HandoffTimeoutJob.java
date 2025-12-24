package com.br.ailinkbiz.Util;

import com.br.ailinkbiz.logging.ConversationLogger;
import com.br.ailinkbiz.model.ConversationState;
import com.br.ailinkbiz.model.DecisionSource;
import com.br.ailinkbiz.store.ConversationStore;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Component
public class HandoffTimeoutJob {

    // tempo de inatividade
    private static final Duration TIMEOUT = Duration.ofMinutes(1);

    private final ConversationStore conversationStore;

    private final ConversationLogger conversationLogger;


    public HandoffTimeoutJob(
            ConversationStore conversationStore,
            ConversationLogger conversationLogger
    ) {
        this.conversationStore = conversationStore;
        this.conversationLogger = conversationLogger;
    }

    @Scheduled(fixedDelay = 60_000) // roda a cada 1 minuto
    public void checkHandoffTimeouts() {

        Instant now = Instant.now();

        // percorre TODAS as conversas conhecidas no Redis
        for (Map.Entry<String, ConversationState> entry :
                conversationStore.getAllStates().entrySet()) {

            String user = entry.getKey();
            ConversationState state = entry.getValue();

            if (state != ConversationState.HUMAN_HANDOFF) continue;

            conversationStore.getLastInteraction(user).ifPresent(last -> {

                if (Duration.between(last, now).compareTo(TIMEOUT) > 0) {

                    conversationStore.saveConversation(
                            user,
                            ConversationState.NEW
                    );

                    conversationLogger.logTurn(
                            "DEFAULT", // clientId (fixo por enquanto)
                            user,
                            "DEFAULT", // flow
                            ConversationState.HUMAN_HANDOFF.name(), // step anterior
                            "SYSTEM_TIMEOUT",
                            "Atendimento encerrado automaticamente por inatividade.",
                            DecisionSource.SYSTEM
                    );

                }
            });
        }
    }
}
