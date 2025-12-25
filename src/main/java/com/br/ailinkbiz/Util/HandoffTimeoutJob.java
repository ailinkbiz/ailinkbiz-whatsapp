package com.br.ailinkbiz.Util;

import com.br.ailinkbiz.logging.ConversationLogger;
import com.br.ailinkbiz.model.ConversationState;
import com.br.ailinkbiz.model.DecisionSource;
import com.br.ailinkbiz.persistence.entity.ConversationClosure;
import com.br.ailinkbiz.repository.ConversationClosureRepository;
import com.br.ailinkbiz.store.ConversationStore;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class HandoffTimeoutJob {

    private static final Duration TIMEOUT = Duration.ofMinutes(1);

    private final ConversationStore conversationStore;
    private final ConversationLogger conversationLogger;
    private final ConversationClosureRepository closureRepository;

    public HandoffTimeoutJob(
            ConversationStore conversationStore,
            ConversationLogger conversationLogger,
            ConversationClosureRepository closureRepository
    ) {
        this.conversationStore = conversationStore;
        this.conversationLogger = conversationLogger;
        this.closureRepository = closureRepository;
    }

    @Scheduled(fixedDelay = 60_000)
    public void checkHandoffTimeouts() {

        Instant now = Instant.now();

        for (var entry : conversationStore.getAllStates().entrySet()) {

            String user = entry.getKey();
            ConversationState state = entry.getValue();

            if (state != ConversationState.HUMAN_HANDOFF) continue;

            conversationStore.getLastInteraction(user).ifPresent(last -> {

                if (Duration.between(last, now).compareTo(TIMEOUT) > 0) {

                    String conversationId =
                            conversationStore.getConversationId(user).orElse(null);

                    String clientId =
                            conversationStore.getClientId(user).orElse("UNKNOWN");

                    if (conversationId != null) {
                        closureRepository.save(
                                new ConversationClosure(
                                        UUID.fromString(conversationId),
                                        clientId,
                                        user,
                                        "SYSTEM_TIMEOUT"
                                )
                        );
                    }

                    conversationLogger.logTurn(
                            conversationId,
                            clientId,
                            user,
                            "DEFAULT",
                            ConversationState.HUMAN_HANDOFF.name(),
                            "SYSTEM_TIMEOUT",
                            "Atendimento encerrado automaticamente por inatividade.",
                            DecisionSource.SYSTEM
                    );

                    conversationStore.clearConversation(user);
                }

            });

        }

    }

}
