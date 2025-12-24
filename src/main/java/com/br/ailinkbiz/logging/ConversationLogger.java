package com.br.ailinkbiz.logging;

import com.br.ailinkbiz.model.DecisionSource;

public interface ConversationLogger {

    void logTurn(
            String clientId,
            String from,
            String flow,
            String step,
            String input,
            String output,
            DecisionSource decisionSource
    );

}