package com.br.ailinkbiz.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversation_closure")
public class ConversationClosure {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID conversationId;

    @Column(nullable = false)
    private String clientId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private Instant closedAt;

    protected ConversationClosure() {}

    public ConversationClosure(
            UUID conversationId,
            String clientId,
            String userId,
            String reason
    ) {
        this.id = UUID.randomUUID();
        this.conversationId = conversationId;
        this.clientId = clientId;
        this.userId = userId;
        this.reason = reason;
        this.closedAt = Instant.now();
    }
}