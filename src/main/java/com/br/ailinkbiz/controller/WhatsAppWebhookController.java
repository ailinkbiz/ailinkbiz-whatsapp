package com.br.ailinkbiz.controller;

import com.br.ailinkbiz.Util.PhoneNormalizer;
import com.br.ailinkbiz.flow.FlowResolver;
import com.br.ailinkbiz.model.ConversationState;
import com.br.ailinkbiz.service.MessageSender;
import com.br.ailinkbiz.store.ConversationStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.br.ailinkbiz.Util.Utils.getSender;

import com.br.ailinkbiz.logging.ConversationLogger;
import com.br.ailinkbiz.model.DecisionSource;

import com.br.ailinkbiz.flow.FlowHandler;
import com.br.ailinkbiz.flow.FlowContext;
import com.br.ailinkbiz.flow.FlowResult;

@RestController
@RequestMapping("/webhook/whatsapp")
public class WhatsAppWebhookController {

    private final ConversationStore conversationStore;
    private final ConversationLogger conversationLogger;
    private final FlowResolver flowResolver;

    public WhatsAppWebhookController(
            ConversationStore conversationStore,
            ConversationLogger conversationLogger,
            FlowResolver flowResolver
    ) {
        this.conversationStore = conversationStore;
        this.conversationLogger = conversationLogger;
        this.flowResolver = flowResolver;
    }

    @PostMapping("/inbound")
    public ResponseEntity<Void> receiveMessage(@RequestParam Map<String, String> payload) {

        //String clientId = "+15551234";
        String clientId = resolveClientId(payload);

        MessageSender sender;
        String resposta = "";
        ConversationState state;
        String body;

        sender = getSender();

        String userId = PhoneNormalizer.toUserId(payload.get("From"));
        String conversationId = conversationStore.getOrCreateConversationId(userId);

        body = payload.getOrDefault("Body", "").trim();

        conversationStore.saveClientId(userId, clientId);

        state = conversationStore
                .getState(userId)
                .orElse(ConversationState.NEW);

        FlowContext context = new FlowContext(
                clientId,
                userId,
                body,
                state
        );

        FlowHandler flowHandler = flowResolver.resolve(clientId);
        FlowResult result = flowHandler.handle(context);

        resposta = result.getOutput();
        ConversationState nextState = result.getNextState();

        if (nextState == null) {
            conversationStore.clearConversation(userId);
        } else {
            conversationStore.saveConversation(userId, nextState);
        }

        conversationLogger.logTurn(
                conversationId,
                clientId,
                userId,
                "DEFAULT",
                state.name(),
                body,
                resposta,
                DecisionSource.RULE
        );

        if (resposta != null && !resposta.isBlank()) {
            sender.send(PhoneNormalizer.toWhatsApp(userId), resposta);
        }

        return ResponseEntity.ok().build();

    }

    private String resolveClientId(Map<String, String> payload) {

        String to = payload.get("To");

        if (to == null || to.isBlank()) {
            return "DEFAULT";
        }

        return to
                .replace("whatsapp:", "")
                .replace(" ", "");
    }

}
