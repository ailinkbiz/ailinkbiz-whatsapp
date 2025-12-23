package com.br.ailinkbiz.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data @AllArgsConstructor
public class ConversationLog {

    private final String user;
    private final String message;
    private final ConversationState state;
    private final Direction direction;
    private final LocalDateTime timestamp = LocalDateTime.now();

    public enum Direction {
        INBOUND,
        OUTBOUND,
        SYSTEM
    }

}