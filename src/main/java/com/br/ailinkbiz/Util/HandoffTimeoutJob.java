package com.br.ailinkbiz.Util;

import com.br.ailinkbiz.model.ConversationLog;
import com.br.ailinkbiz.model.ConversationState;
import com.br.ailinkbiz.store.ConversationStore;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static com.br.ailinkbiz.Util.Utils.addLog;

@Component
public class HandoffTimeoutJob {

    // tempo de inatividade
    private static final Duration TIMEOUT = Duration.ofMinutes(1);

    private final ConversationStore conversationStore;

    public HandoffTimeoutJob(ConversationStore conversationStore) {
        this.conversationStore = conversationStore;
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

                    addLog(
                            user,
                            "Atendimento encerrado automaticamente por inatividade.",
                            ConversationState.NEW,
                            ConversationLog.Direction.SYSTEM
                    );
                }
            });
        }
    }
}
