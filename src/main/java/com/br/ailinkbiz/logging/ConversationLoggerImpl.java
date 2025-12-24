package com.br.ailinkbiz.logging;

import com.br.ailinkbiz.model.DecisionSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ConversationLoggerImpl implements ConversationLogger {

    @Override
    public void logTurn(
            String clientId,
            String from,
            String flow,
            String step,
            String input,
            String output,
            DecisionSource decisionSource
    ) {
        System.out.println(
                "[CONVERSATION_LOG] " +
                        "clientId=" + clientId +
                        " from=" + from +
                        " flow=" + flow +
                        " step=" + step +
                        " input=\"" + input + "\"" +
                        " output=\"" + output + "\"" +
                        " decisionSource=" + decisionSource +
                        " timestamp=" + LocalDateTime.now()
        );
    }

}