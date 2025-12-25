package com.br.ailinkbiz.controller;

import com.br.ailinkbiz.Util.PhoneNormalizer;
import com.br.ailinkbiz.flow.FlowResolver;
import com.br.ailinkbiz.model.ConversationState;
import com.br.ailinkbiz.persistence.entity.ConversationClosure;
import com.br.ailinkbiz.repository.ConversationClosureRepository;
import com.br.ailinkbiz.service.MessageSender;
import com.br.ailinkbiz.store.ConversationStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

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
    private final ConversationClosureRepository closureRepository;

    public WhatsAppWebhookController(
            ConversationStore conversationStore,
            ConversationLogger conversationLogger,
            FlowResolver flowResolver,
            ConversationClosureRepository closureRepository
    ) {
        this.conversationStore = conversationStore;
        this.conversationLogger = conversationLogger;
        this.flowResolver = flowResolver;
        this.closureRepository = closureRepository;
    }

    @PostMapping("/inbound")
    public ResponseEntity<Void> receiveMessage(@RequestParam Map<String, String> payload) {

        MessageSender sender = getSender();

        String clientId = resolveClientId(payload);
        String userId = PhoneNormalizer.toUserId(payload.get("From"));
        String body = payload.getOrDefault("Body", "").trim();

        // 🔑 reutiliza conversa ativa ou cria nova
        String conversationId =
                conversationStore
                        .getActiveConversationId(clientId, userId)
                        .orElseGet(() -> {
                            String id = conversationStore.createConversation(userId, clientId);
                            conversationStore.setActiveConversation(clientId, userId, id);
                            return id;
                        });

        ConversationState currentState =
                conversationStore
                        .getStateByConversation(conversationId)
                        .orElse(ConversationState.NEW);

        FlowContext context = new FlowContext(
                clientId,
                userId,
                body,
                currentState
        );

        FlowHandler flowHandler = flowResolver.resolve(clientId);
        FlowResult result = flowHandler.handle(context);

        String resposta = result.getOutput();
        ConversationState nextState = result.getNextState();

        if (nextState == null) {

            // encerramento normal do fluxo
            closureRepository.save(
                    new ConversationClosure(
                            UUID.fromString(conversationId),
                            clientId,
                            userId,
                            "FLOW_COMPLETED"
                    )
            );

            conversationStore.closeConversation(conversationId, "FLOW_COMPLETED");
            conversationStore.clearActiveConversation(clientId, userId);

        } else {

            conversationStore.updateState(conversationId, nextState);

            if (nextState == ConversationState.HUMAN_HANDOFF) {
                conversationStore.markHandoff(conversationId);
            }
        }

        conversationLogger.logTurn(
                conversationId,
                clientId,
                userId,
                "DEFAULT",
                currentState.name(),
                body,
                resposta,
                DecisionSource.RULE
        );

        if (resposta != null && !resposta.isBlank()) {
            sender.send(
                    PhoneNormalizer.toWhatsApp(userId),
                    resposta
            );
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
