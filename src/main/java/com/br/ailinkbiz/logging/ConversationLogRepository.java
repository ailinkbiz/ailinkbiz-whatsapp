package com.br.ailinkbiz.logging;

import com.br.ailinkbiz.model.ConversationLog;

public interface ConversationLogRepository {
    void save(ConversationLog log);
}
