package com.br.ailinkbiz.dto;

import java.time.LocalDateTime;

public record ConversationOverviewDTO(

        String conversationId,
        String user,               // mascarado: +55 •••• 6809
        String status,             // ATIVA | EM_ATENDIMENTO | ENCERRADA
        String closeReason,        // null se não encerrada
        LocalDateTime lastActivity // sempre no fuso do cliente

) {}
