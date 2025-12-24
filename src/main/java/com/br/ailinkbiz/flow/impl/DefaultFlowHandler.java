package com.br.ailinkbiz.flow.impl;

import com.br.ailinkbiz.flow.FlowContext;
import com.br.ailinkbiz.flow.FlowHandler;
import com.br.ailinkbiz.flow.FlowResult;
import com.br.ailinkbiz.model.ConversationState;
import org.springframework.stereotype.Component;

@Component
public class DefaultFlowHandler implements FlowHandler {

    @Override
    public FlowResult handle(FlowContext context) {

        ConversationState state = context.getState();
        String input = context.getInput();

        return switch (state) {

            case NEW -> handleNew();

            case WAITING_OPTION -> handleWaitingOption(input);

            case HUMAN_HANDOFF -> handleHumanHandoff();

            default -> new FlowResult(
                    "Algo deu errado. Vamos recomeçar.",
                    ConversationState.NEW
            );

        };
    }

    private FlowResult handleNew() {

        String output =
                "Olá! 👋\n" +
                        "Sou o atendimento automático da AiLinkBiz.\n\n" +
                        "Como posso te ajudar?\n\n" +
                        "1️⃣ Falar com atendimento\n" +
                        "2️⃣ Horário de funcionamento\n" +
                        "3️⃣ Endereço";

        return new FlowResult(output, ConversationState.WAITING_OPTION);

    }

    private FlowResult handleWaitingOption(String input) {

        return switch (input) {

            case "1" -> new FlowResult(
                    "Perfeito! 👤\n" +
                            "Um atendente humano vai assumir a conversa a partir de agora.\n\n" +
                            "Por favor, aguarde.",
                    ConversationState.HUMAN_HANDOFF
            );

            case "2" -> new FlowResult(
                    "Nosso horário é de segunda a sexta, das 9h às 18h.",
                    null
            );

            case "3" -> new FlowResult(
                    "Estamos localizados na Rua X, número Y.",
                    null
            );

            default -> new FlowResult(
                    "Não entendi 😕\nResponda com 1, 2 ou 3.",
                    ConversationState.WAITING_OPTION
            );

        };
    }

    private FlowResult handleHumanHandoff() {
        return new FlowResult(null, null);
    }
}