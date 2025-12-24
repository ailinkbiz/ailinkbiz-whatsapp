package com.br.ailinkbiz.dto;

import java.time.LocalDateTime;

public record HandoffConversationDTO(String user, String lastMessage, LocalDateTime lastMessageAt) {

}