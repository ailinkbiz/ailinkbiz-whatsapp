package com.br.ailinkbiz.flow.impl;

import com.br.ailinkbiz.flow.FlowContext;
import com.br.ailinkbiz.flow.FlowHandler;
import com.br.ailinkbiz.flow.FlowResult;
import com.br.ailinkbiz.model.ConversationState;
import org.springframework.stereotype.Component;

@Component
public class SimpleInfoFlowHandler implements FlowHandler {

    @Override
    public FlowResult handle(FlowContext context) {

        // ignora completamente estado e input
        // comportamento totalmente diferente
        return new FlowResult(
                "Olá! 👋\n" +
                        "Este é um fluxo simples.\n\n" +
                        "Aqui não existe menu, estado ou handoff.\n" +
                        "Cada mensagem recebe sempre essa resposta.",
                null
        );
    }
}