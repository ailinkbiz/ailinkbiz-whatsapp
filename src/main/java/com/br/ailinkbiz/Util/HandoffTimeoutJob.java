package com.br.ailinkbiz.Util;

import com.br.ailinkbiz.logging.ConversationLogger;
import com.br.ailinkbiz.model.DecisionSource;
import com.br.ailinkbiz.persistence.entity.ConversationClosure;
import com.br.ailinkbiz.repository.ConversationClosureRepository;
import com.br.ailinkbiz.store.ConversationStore;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
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

        // 🔑 Fonte da verdade: TODAS as conversas em HANDOFF
        Set<String> handoffConversationIds =
                conversationStore.getAllHandoffConversationIds();

        for (String conversationId : handoffConversationIds) {

            Map<Object, Object> data =
                    conversationStore.getConversationData(conversationId);

            Object lastInteractionObj = data.get("lastInteraction");
            Object clientIdObj = data.get("clientId");
            Object userObj = data.get("phone");

            if (lastInteractionObj == null || clientIdObj == null) {
                continue;
            }

            Instant lastInteraction = Instant.parse(lastInteractionObj.toString());
            String clientId = clientIdObj.toString();
            String user = userObj != null ? userObj.toString() : "UNKNOWN";

            if (Duration.between(lastInteraction, now).compareTo(TIMEOUT) <= 0) {
                continue;
            }

            // 🔒 auditoria de fechamento
            closureRepository.save(
                    new ConversationClosure(
                            UUID.fromString(conversationId),
                            clientId,
                            user,
                            "SYSTEM_TIMEOUT"
                    )
            );

            conversationLogger.logTurn(
                    conversationId,
                    clientId,
                    user,
                    "DEFAULT",
                    "HANDOFF",
                    "SYSTEM_TIMEOUT",
                    "Atendimento encerrado automaticamente por inatividade.",
                    DecisionSource.SYSTEM
            );

            // 🔑 fechamento canônico no domínio
            conversationStore.closeConversation(
                    conversationId,
                    "SYSTEM_TIMEOUT"
            );
        }
    }
}
