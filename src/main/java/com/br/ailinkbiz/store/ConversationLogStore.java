package com.br.ailinkbiz.store;

import com.br.ailinkbiz.model.ConversationLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ConversationLogStore
 *
 * Log operacional em memória.
 * - NÃO é fonte da verdade
 * - NÃO garante persistência
 * - Pode ser limpo a qualquer momento
 * - Usado apenas para observabilidade/debug
 */
public class ConversationLogStore {

    private static final List<ConversationLog> LOGS = Collections.synchronizedList(new ArrayList<>());

    public static void add(ConversationLog log) {
        LOGS.add(log);
        //System.out.println(log);
    }

    public static List<ConversationLog> getAll() {
        return List.copyOf(LOGS);
    }

    public static void clear() {
        LOGS.clear();
    }

}