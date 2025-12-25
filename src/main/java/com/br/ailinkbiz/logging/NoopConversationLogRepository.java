package com.br.ailinkbiz.logging;

import com.br.ailinkbiz.model.ConversationLog;
import org.springframework.stereotype.Component;

@Component
public class NoopConversationLogRepository implements ConversationLogRepository {

    @Override
    public void save(ConversationLog log) {
        // intencionalmente vazio
        // banco ainda não está ativo
    }
}