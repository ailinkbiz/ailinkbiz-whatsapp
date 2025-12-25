package com.br.ailinkbiz.repository;

import com.br.ailinkbiz.persistence.entity.ConversationClosure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConversationClosureRepository
        extends JpaRepository<ConversationClosure, UUID> {
}
