package com.br.ailinkbiz.controller;

import com.br.ailinkbiz.dto.ConversationOverviewDTO;
import com.br.ailinkbiz.service.ConversationOverviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/conversations")
public class ConversationOverviewController {

    private final ConversationOverviewService overviewService;

    public ConversationOverviewController(
            ConversationOverviewService overviewService
    ) {
        this.overviewService = overviewService;
    }

    @GetMapping
    public List<ConversationOverviewDTO> listConversations(
            @RequestParam String clientId
    ) {
        return overviewService.getOverview(clientId);
    }
}
