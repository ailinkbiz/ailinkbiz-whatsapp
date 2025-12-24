package com.br.ailinkbiz.controller;

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

        String from = normalizeFrom(payload);
        body = payload.getOrDefault("Body", "").trim();

        state = conversationStore
                .getState(from)
                .orElse(ConversationState.NEW);

        FlowContext context = new FlowContext(
                clientId,
                from,
                body,
                state
        );

        FlowHandler flowHandler = flowResolver.resolve(clientId);
        FlowResult result = flowHandler.handle(context);

        resposta = result.getOutput();
        ConversationState nextState = result.getNextState();

        if (nextState == null) {
            conversationStore.clearConversation(from);
        } else {
            conversationStore.saveConversation(from, nextState);
        }

        conversationLogger.logTurn(
                clientId,          // clientId (fixo por enquanto)
                from,
                "DEFAULT",          // flow (fixo por enquanto)
                state.name(),       // step atual
                body,               // input
                resposta,           // output
                DecisionSource.RULE // decisão por regra
        );

        if (resposta != null && !resposta.isBlank()) {
            sender.send(from, resposta);
        }

        return ResponseEntity.ok().build();

    }

//    private String handleNew(String from) {
//
//        String resposta =
//                "Olá! 👋\n" +
//                "Sou o atendimento automático da AiLinkBiz.\n\n" +
//                "Como posso te ajudar?\n\n" +
//                "1️⃣ Falar com atendimento\n" +
//                "2️⃣ Horário de funcionamento\n" +
//                "3️⃣ Endereço";
//
//        conversationStore.saveConversation(from, ConversationState.WAITING_OPTION);
//
//        return resposta;
//
//    }
//
//    public String handleWaitingOption(String from, String body) {
//
//        String resposta;
//
//        switch (body) {
//
//            case "1" -> {
//
//                resposta =
//                        "Perfeito! 👤\n" +
//                        "Um atendente humano vai assumir a conversa a partir de agora.\n\n" +
//                        "Por favor, aguarde.";
//
//                conversationStore.saveConversation(from, ConversationState.HUMAN_HANDOFF);
//
//            }
//
//            case "2" -> {
//
//                resposta = "Nosso horário é de segunda a sexta, das 9h às 18h.";
//
//                conversationStore.clearConversation(from);
//
//            }
//
//            case "3" -> {
//
//                resposta = "Estamos localizados na Rua X, número Y.";
//
//                conversationStore.clearConversation(from);
//
//            }
//
//            default -> {
//
//                resposta = "Não entendi 😕\nResponda com 1, 2 ou 3.";
//
//            }
//
//        }
//
//        return resposta;
//
//    }
//
//    private String handleHumanHandoff(String from) {
//        System.out.println("Mensagem recebida em handoff humano de: " + from);
//        return null;
//    }

    private String normalizeFrom(Map<String, String> payload) {

        String from = payload.get("From").trim()
                .replace("whatsapp:", "")
                .replace(" ", "");

        if (!from.startsWith("+")) {
            from = "+" + from;
        }

        return "whatsapp:" + from;
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
