package com.br.ailinkbiz.dto;

import java.time.LocalDateTime;

public record UserConversationOverviewDTO(
        String user,
        String status,
        LocalDateTime lastActivity
) {}
