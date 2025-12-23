package com.br.ailinkbiz.store;

import com.br.ailinkbiz.model.ConversationLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConversationLogStore {

    private static final List<ConversationLog> LOGS =
            Collections.synchronizedList(new ArrayList<>());

    public static void add(ConversationLog log) {
        LOGS.add(log);
        System.out.println(log);
    }

    public static List<ConversationLog> getAll() {
        return List.copyOf(LOGS);
    }

}