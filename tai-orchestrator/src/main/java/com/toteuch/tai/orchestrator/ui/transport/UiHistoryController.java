// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.transport;

import com.toteuch.tai.orchestrator.ui.history.ConversationHistoryService;
import com.toteuch.tai.orchestrator.ui.model.history.ConversationHistoryPage;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ui/history")
public class UiHistoryController {

    private final ConversationHistoryService conversationHistoryService;

    public UiHistoryController(ConversationHistoryService conversationHistoryService) {
        this.conversationHistoryService = conversationHistoryService;
    }

    @GetMapping
    public ConversationHistoryPage getHistory(
            @Parameter(
                            description = "Maximum number of turns to return",
                            schema = @Schema(defaultValue = "20", minimum = "1", maximum = "100"))
                    @RequestParam(name = "limit", required = false)
                    Integer limit,
            @Parameter(description = "Correlation id cursor returned by the previous history page")
                    @RequestParam(name = "cursor", required = false)
                    String cursor) {
        return conversationHistoryService.getHistory(limit, cursor);
    }
}
