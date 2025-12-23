package com.br.ailinkbiz.controller;

import com.br.ailinkbiz.model.ConversationLog;
import com.br.ailinkbiz.model.ConversationState;
import com.br.ailinkbiz.service.MessageSender;
import com.br.ailinkbiz.store.ConversationStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.br.ailinkbiz.Util.Utils.addLog;
import static com.br.ailinkbiz.Util.Utils.getSender;
import static com.br.ailinkbiz.model.ConversationState.WAITING_OPTION;

@RestController
@RequestMapping("/webhook/whatsapp")
public class WhatsAppWebhookController {

    private final ConversationStore conversationStore;

    private MessageSender sender;
    private String resposta = "";

    public WhatsAppWebhookController(ConversationStore conversationStore) {
        this.conversationStore = conversationStore;
    }

    @PostMapping("/inbound")
    public ResponseEntity<Void> receiveMessage(@RequestParam Map<String, String> payload) {

        sender = getSender();

        String from = normalizeFrom(payload);
        String body = payload.getOrDefault("Body", "").trim();

        ConversationState state = conversationStore
                .getState(from)
                .orElse(ConversationState.NEW);

        addLog(from, body, state, ConversationLog.Direction.INBOUND);

        switch (state) {
            case NEW -> handleNew(from);
            case WAITING_OPTION -> handleWaitingOption(from, body);
            case HUMAN_HANDOFF -> handleHumanHandoff(from);
        }

        return ResponseEntity.ok().build();
    }

    private void handleNew(String from) {

        resposta =
                "Olá! 👋\n" +
                        "Sou o atendimento automático da AiLinkBiz.\n\n" +
                        "Como posso te ajudar?\n\n" +
                        "1️⃣ Falar com atendimento\n" +
                        "2️⃣ Horário de funcionamento\n" +
                        "3️⃣ Endereço";

        sender.send(from, resposta);

        conversationStore.saveConversation(from, ConversationState.WAITING_OPTION);

        addLog(
                from,
                resposta,
                ConversationState.WAITING_OPTION,
                ConversationLog.Direction.OUTBOUND
        );
    }

    public void handleWaitingOption(String from, String body) {

        switch (body) {

            case "1" -> {

                resposta =
                        "Perfeito! 👤\n" +
                                "Um atendente humano vai assumir a conversa a partir de agora.\n\n" +
                                "Por favor, aguarde.";

                sender.send(from, resposta);

                conversationStore.saveConversation(from, ConversationState.HUMAN_HANDOFF);

                addLog(
                        from,
                        resposta,
                        ConversationState.HUMAN_HANDOFF,
                        ConversationLog.Direction.OUTBOUND
                );
            }

            case "2" -> {

                resposta = "Nosso horário é de segunda a sexta, das 9h às 18h.";

                sender.send(from, resposta);

                conversationStore.clearConversation(from);

                addLog(
                        from,
                        resposta,
                        ConversationState.NEW,
                        ConversationLog.Direction.OUTBOUND
                );
            }

            case "3" -> {

                resposta = "Estamos localizados na Rua X, número Y.";

                sender.send(from, resposta);

                conversationStore.clearConversation(from);

                addLog(
                        from,
                        resposta,
                        ConversationState.NEW,
                        ConversationLog.Direction.OUTBOUND
                );
            }

            default -> {

                resposta = "Não entendi 😕\nResponda com 1, 2 ou 3.";

                sender.send(from, resposta);

                addLog(
                        from,
                        resposta,
                        WAITING_OPTION,
                        ConversationLog.Direction.OUTBOUND
                );
            }
        }
    }

    private void handleHumanHandoff(String from) {
        System.out.println("Mensagem recebida em handoff humano de: " + from);
    }

    @GetMapping("/test-send")
    public String testSend() {
        sender = getSender();
        sender.send(
                "whatsapp:+5511965886809",
                "Teste direto de envio"
        );
        return "ok";
    }

    private String normalizeFrom(Map<String, String> payload) {

        String from = payload.get("From").trim()
                .replace("whatsapp:", "")
                .replace(" ", "");

        if (!from.startsWith("+")) {
            from = "+" + from;
        }

        return "whatsapp:" + from;
    }
}


//package com.br.ailinkbiz.controller;
//
//import com.br.ailinkbiz.model.ConversationLog;
//import com.br.ailinkbiz.model.ConversationState;
//import com.br.ailinkbiz.service.MessageSender;
//import com.br.ailinkbiz.store.ConversationStore;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import java.util.Map;
//
//import static com.br.ailinkbiz.Util.Utils.addLog;
//import static com.br.ailinkbiz.Util.Utils.getSender;
//import static com.br.ailinkbiz.model.ConversationState.WAITING_OPTION;
//
//@RestController
//@RequestMapping("/webhook/whatsapp")
//public class WhatsAppWebhookController {
//
//    String resposta = "";
//    MessageSender sender;
//
//    @PostMapping("/inbound")
//    public ResponseEntity<Void> receiveMessage(@RequestParam Map<String, String> payload) {
//
//        sender = getSender();
//
//        String from = normalizeFrom(payload);
//        String body = payload.getOrDefault("Body", "").trim();
//
//        ConversationStore.touch(from);
//
//        // Busca estado atual
//        ConversationState state = ConversationStore.get(from);
//
//        // Armazena log
//        addLog(from, body, state, ConversationLog.Direction.INBOUND);
//
//        // Decide o que fazer
//        switch (state) {
//            case NEW -> handleNew(from);
//            case WAITING_OPTION -> handleWaitingOption(from, body);
//            case HUMAN_HANDOFF -> handleHumanHandoff(from);
//        }
//
//        return ResponseEntity.ok().build();
//
//    }
//
//    private void handleNew(String from) {
//
//        resposta =
//                "Olá! 👋\n" +
//                        "Sou o atendimento automático da AiLinkBiz.\n\n" +
//                        "Como posso te ajudar?\n\n" +
//                        "1️⃣ Falar com atendimento\n" +
//                        "2️⃣ Horário de funcionamento\n" +
//                        "3️⃣ Endereço";
//
//        sender.send(from, resposta);
//        ConversationStore.set(from, ConversationState.WAITING_OPTION);
//
//        // Armazena log
//        addLog(from, resposta, ConversationState.WAITING_OPTION, ConversationLog.Direction.OUTBOUND);
//
//    }
//
//    public void handleWaitingOption(String from, String body){
//
//        switch (body) {
//
//            case "1" -> {
//
//                resposta = "Perfeito! 👤\n" +
//                        "Um atendente humano vai assumir a conversa a partir de agora.\n\n" +
//                        "Por favor, aguarde.";
//
//                sender.send(from, resposta);
//
//                ConversationStore.set(from, ConversationState.HUMAN_HANDOFF);
//
//                // Armazena log
//                addLog(from, resposta, ConversationState.HUMAN_HANDOFF, ConversationLog.Direction.OUTBOUND);
//
//            }
//
//            case "2" -> {
//
//                resposta = "Nosso horário é de segunda a sexta, das 9h às 18h.";
//
//                sender.send(from, resposta);
//                ConversationStore.clear(from);
//
//                // Armazena log
//                addLog(from, resposta, ConversationState.NEW, ConversationLog.Direction.OUTBOUND);
//
//            }
//
//            case "3" -> {
//
//                resposta = "Estamos localizados na Rua X, número Y.";
//
//                sender.send(from, resposta);
//                ConversationStore.clear(from);
//
//                // Armazena log
//                addLog(from, resposta, ConversationState.NEW, ConversationLog.Direction.OUTBOUND);
//
//            }
//
//            default -> {
//
//                resposta = "Não entendi 😕\nResponda com 1, 2 ou 3.";
//
//                sender.send(from,resposta);
//
//                // Armazena log
//                addLog(from, resposta, WAITING_OPTION, ConversationLog.Direction.OUTBOUND);
//
//            }
//
//        }
//
//    }
//
//    private void handleHumanHandoff(String from) {
//        System.out.println("Mensagem recebida em handoff humano de: " + from);
//    }
//
//    @GetMapping("/test-send")
//    public String testSend() {
//        sender.send(
//                "whatsapp:+5511965886809",
//                "Teste direto de envio"
//        );
//        return "ok";
//    }
//
//    private String normalizeFrom(Map<String, String> payload) {
//        String from = payload.get("From").trim()
//                .replace("whatsapp:", "")
//                .replace(" ", "");
//
//        if (!from.startsWith("+")) {
//            from = "+" + from;
//        }
//
//        return "whatsapp:" + from;
//    }
//
//}
//
