package com.br.ailinkbiz.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ConversationLog {

    private String logId;

    // Identifica a conversa
    private String conversationId;

    // Identidade
    private String clientId;
    private String userId; // telefone normalizado

    // Contexto conversacional
    private String flow; // DEFAULT, SALES, SUPPORT...
    private String step; // MENU, ASK_NAME, CONFIRM...

    // Conteúdo
    private String input;   // mensagem recebida
    private String output;  // mensagem enviada

    // Decisão
    private DecisionSource decisionSource; // RULE | N8N | AI | HUMAN

    // Metadata
    private Instant timestamp;

}