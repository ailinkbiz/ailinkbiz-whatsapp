package com.br.ailinkbiz.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data @AllArgsConstructor
public class ConversationLog {

    private String logId;

    // Identidade
    private String clientId;
    private String from; // telefone normalizado

    // Contexto conversacional
    private String flow; // DEFAULT, SALES, SUPPORT...
    private String step; // MENU, ASK_NAME, CONFIRM...

    // Conteúdo
    private String input;   // mensagem recebida
    private String output;  // mensagem enviada

    // Decisão
    private DecisionSource decisionSource; // RULE | N8N | AI | HUMAN

    // Metadata
    private LocalDateTime timestamp;

    public enum Direction {
        INBOUND,
        OUTBOUND,
        SYSTEM
    }

}
//public class ConversationLog {
//
//    private final String user;
//    private final String message;
//    private final ConversationState state;
//    private final Direction direction;
//    private final LocalDateTime timestamp = LocalDateTime.now();
//
//    public enum Direction {
//        INBOUND,
//        OUTBOUND,
//        SYSTEM
//    }
//
//}