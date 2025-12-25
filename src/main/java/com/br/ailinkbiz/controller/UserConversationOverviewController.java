package com.br.ailinkbiz.controller;

import com.br.ailinkbiz.dto.UserConversationOverviewDTO;
import com.br.ailinkbiz.service.UserConversationOverviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/overview/users")
public class UserConversationOverviewController {

    private final UserConversationOverviewService overviewService;

    public UserConversationOverviewController(
            UserConversationOverviewService overviewService
    ) {
        this.overviewService = overviewService;
    }

    @GetMapping
    public List<UserConversationOverviewDTO> getOverview(
            @RequestParam String clientId
    ) {
        return overviewService.getOverview(clientId);
    }
}
