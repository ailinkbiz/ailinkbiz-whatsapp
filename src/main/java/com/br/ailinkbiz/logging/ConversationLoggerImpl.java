package com.br.ailinkbiz.logging;

import com.br.ailinkbiz.model.ConversationLog;
import com.br.ailinkbiz.model.DecisionSource;
import com.br.ailinkbiz.store.ConversationLogStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class ConversationLoggerImpl implements ConversationLogger {

    @Override
    public void logTurn(
            String conversationId,
            String clientId,
            String userId,
            String flow,
            String step,
            String input,
            String output,
            DecisionSource decisionSource
    ) {

        ConversationLog log = new ConversationLog(
                UUID.randomUUID().toString(), // logId (volátil)
                conversationId,
                clientId,
                userId,
                flow,
                step,
                input,
                output,
                decisionSource,
                Instant.now()
        );

        // Observabilidade
        System.out.println(log);

        // Log operacional (volátil)
        ConversationLogStore.add(log);
    }

}