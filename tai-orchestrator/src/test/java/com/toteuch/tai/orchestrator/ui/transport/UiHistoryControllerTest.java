// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.transport;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.toteuch.tai.orchestrator.ui.history.ConversationHistoryService;
import com.toteuch.tai.orchestrator.ui.model.history.ConversationHistoryItem;
import com.toteuch.tai.orchestrator.ui.model.history.ConversationHistoryPage;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UiHistoryControllerTest {

    private static final Instant GENERATED_AT = Instant.parse("2026-05-01T10:00:00Z");
    private static final Instant STARTED_AT = Instant.parse("2026-05-01T09:59:50Z");
    private static final Instant COMPLETED_AT = Instant.parse("2026-05-01T09:59:58Z");

    @Mock private ConversationHistoryService conversationHistoryService;

    private MockMvc mockMvc;

    @BeforeEach
    void set_up() {
        UiHistoryController controller = new UiHistoryController(conversationHistoryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void get_history_without_parameters_should_return_initial_history_page() throws Exception {
        ConversationHistoryPage page =
                new ConversationHistoryPage(
                        GENERATED_AT,
                        20,
                        "cursor-turn-80",
                        List.of(
                                new ConversationHistoryItem(
                                        "correlation-id-turn-99",
                                        "Hello Tai",
                                        "Hello. How can I help?",
                                        null,
                                        STARTED_AT,
                                        COMPLETED_AT)));

        when(conversationHistoryService.getHistory(null, null)).thenReturn(page);

        mockMvc.perform(get("/ui/history").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generatedAt").value("2026-05-01T10:00:00Z"))
                .andExpect(jsonPath("$.limit").value(20))
                .andExpect(jsonPath("$.nextCursor").value("cursor-turn-80"))
                .andExpect(jsonPath("$.items[0].correlationId").value("correlation-id-turn-99"))
                .andExpect(jsonPath("$.items[0].userText").value("Hello Tai"))
                .andExpect(jsonPath("$.items[0].assistantText").value("Hello. How can I help?"))
                .andExpect(jsonPath("$.items[0].startedAt").value("2026-05-01T09:59:50Z"))
                .andExpect(jsonPath("$.items[0].completedAt").value("2026-05-01T09:59:58Z"));

        verify(conversationHistoryService).getHistory(null, null);
    }

    @Test
    void get_history_with_limit_and_cursor_should_delegate_to_service() throws Exception {
        ConversationHistoryPage page =
                new ConversationHistoryPage(
                        GENERATED_AT,
                        20,
                        "cursor-turn-60",
                        List.of(
                                new ConversationHistoryItem(
                                        "correlation-id-turn-79",
                                        "Older user message",
                                        "Older assistant message",
                                        null,
                                        STARTED_AT,
                                        COMPLETED_AT)));

        when(conversationHistoryService.getHistory(20, "cursor-turn-80")).thenReturn(page);

        mockMvc.perform(
                        get("/ui/history")
                                .queryParam("limit", "20")
                                .queryParam("cursor", "cursor-turn-80")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generatedAt").value("2026-05-01T10:00:00Z"))
                .andExpect(jsonPath("$.limit").value(20))
                .andExpect(jsonPath("$.nextCursor").value("cursor-turn-60"))
                .andExpect(jsonPath("$.items[0].correlationId").value("correlation-id-turn-79"))
                .andExpect(jsonPath("$.items[0].userText").value("Older user message"))
                .andExpect(jsonPath("$.items[0].assistantText").value("Older assistant message"));

        verify(conversationHistoryService).getHistory(20, "cursor-turn-80");
    }

    @Test
    void get_history_should_return_empty_page_when_service_returns_no_items() throws Exception {
        ConversationHistoryPage page =
                new ConversationHistoryPage(GENERATED_AT, 20, null, List.of());

        when(conversationHistoryService.getHistory(20, null)).thenReturn(page);

        mockMvc.perform(
                        get("/ui/history")
                                .queryParam("limit", "20")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generatedAt").value("2026-05-01T10:00:00Z"))
                .andExpect(jsonPath("$.limit").value(20))
                .andExpect(jsonPath("$.nextCursor").doesNotExist())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty());

        verify(conversationHistoryService).getHistory(20, null);
    }
}
