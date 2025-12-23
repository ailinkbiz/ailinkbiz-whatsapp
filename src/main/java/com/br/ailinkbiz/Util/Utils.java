package com.br.ailinkbiz.Util;

import com.br.ailinkbiz.model.ConversationLog;
import com.br.ailinkbiz.model.ConversationState;
import com.br.ailinkbiz.service.MessageSender;
import com.br.ailinkbiz.service.MockSender;
import com.br.ailinkbiz.service.TwilioSender;
import com.br.ailinkbiz.store.ConversationLogStore;

public class Utils {

    public static MessageSender getSender() {

        MessageSender sender;

        if ("true".equalsIgnoreCase(System.getenv("MOCK_WHATSAPP"))) {
            sender = new MockSender();
        } else {
            sender = new TwilioSender();
        }

        return sender;

    }


    public static void addLog(String from, String message, ConversationState state, ConversationLog.Direction direction) {

        ConversationLogStore.add(
                new ConversationLog(
                        from,
                        message,
                        state,
                        direction
                )
        );

    }

}
