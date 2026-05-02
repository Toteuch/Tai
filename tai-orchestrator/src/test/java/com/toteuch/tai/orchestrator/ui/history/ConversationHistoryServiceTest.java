// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.toteuch.tai.orchestrator.session.ConversationTurn;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SessionStore;
import com.toteuch.tai.orchestrator.ui.model.history.ConversationHistoryItem;
import com.toteuch.tai.orchestrator.ui.model.history.ConversationHistoryPage;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class ConversationHistoryServiceTest {

    private static final Instant NOW = Instant.parse("2026-05-01T10:00:00Z");
    private static final Instant STARTED_AT = Instant.parse("2026-05-01T09:59:50Z");
    private static final Instant COMPLETED_AT = Instant.parse("2026-05-01T09:59:58Z");

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private SessionStore sessionStore;
    private ConversationHistoryMapper mapper;
    private ConversationHistoryService service;

    @BeforeEach
    void set_up() {
        mapper = org.mockito.Mockito.mock(ConversationHistoryMapper.class);
    }

    @Test
    void get_history_without_cursor_should_return_latest_turns_except_last_one() {
        List<ConversationTurn> turns = turns(100);
        given_history(turns);

        ConversationHistoryPage page = service.getHistory(20, null);

        assertThat(page.generatedAt()).isEqualTo(NOW);
        assertThat(page.limit()).isEqualTo(20);
        assertThat(page.nextCursor()).isEqualTo("turn-80");

        assertThat(page.items())
                .extracting(ConversationHistoryItem::correlationId)
                .containsExactly(
                        "turn-99", "turn-98", "turn-97", "turn-96", "turn-95", "turn-94", "turn-93",
                        "turn-92", "turn-91", "turn-90", "turn-89", "turn-88", "turn-87", "turn-86",
                        "turn-85", "turn-84", "turn-83", "turn-82", "turn-81", "turn-80");
    }

    @Test
    void get_history_with_cursor_should_return_older_turns_excluding_cursor_turn() {
        List<ConversationTurn> turns = turns(100);
        given_history(turns);

        ConversationHistoryPage page = service.getHistory(20, "turn-80");

        assertThat(page.generatedAt()).isEqualTo(NOW);
        assertThat(page.limit()).isEqualTo(20);
        assertThat(page.nextCursor()).isEqualTo("turn-60");

        assertThat(page.items())
                .extracting(ConversationHistoryItem::correlationId)
                .containsExactly(
                        "turn-79", "turn-78", "turn-77", "turn-76", "turn-75", "turn-74", "turn-73",
                        "turn-72", "turn-71", "turn-70", "turn-69", "turn-68", "turn-67", "turn-66",
                        "turn-65", "turn-64", "turn-63", "turn-62", "turn-61", "turn-60");
    }

    @Test
    void get_history_should_return_null_next_cursor_on_last_page() {
        List<ConversationTurn> turns = turns(100);
        given_history(turns);

        ConversationHistoryPage page = service.getHistory(20, "turn-20");

        assertThat(page.generatedAt()).isEqualTo(NOW);
        assertThat(page.limit()).isEqualTo(20);
        assertThat(page.nextCursor()).isNull();

        assertThat(page.items())
                .extracting(ConversationHistoryItem::correlationId)
                .containsExactly(
                        "turn-19", "turn-18", "turn-17", "turn-16", "turn-15", "turn-14", "turn-13",
                        "turn-12", "turn-11", "turn-10", "turn-9", "turn-8", "turn-7", "turn-6",
                        "turn-5", "turn-4", "turn-3", "turn-2", "turn-1");
    }

    @Test
    void get_history_should_return_empty_page_when_history_is_empty() {
        given_history(List.of());

        ConversationHistoryPage page = service.getHistory(20, null);

        assertThat(page.generatedAt()).isEqualTo(NOW);
        assertThat(page.limit()).isEqualTo(20);
        assertThat(page.nextCursor()).isNull();
        assertThat(page.items()).isEmpty();
    }

    @Test
    void get_history_should_return_empty_page_when_only_latest_turn_exists() {
        List<ConversationTurn> turns = turns(1);
        given_history(turns);

        ConversationHistoryPage page = service.getHistory(20, null);

        assertThat(page.generatedAt()).isEqualTo(NOW);
        assertThat(page.limit()).isEqualTo(20);
        assertThat(page.nextCursor()).isNull();
        assertThat(page.items()).isEmpty();
    }

    @Test
    void get_history_should_clamp_limit_to_maximum() {
        List<ConversationTurn> turns = turns(150);
        given_history(turns);

        ConversationHistoryPage page = service.getHistory(200, null);

        assertThat(page.limit()).isEqualTo(100);
        assertThat(page.items()).hasSize(100);
        assertThat(page.items().getFirst().correlationId()).isEqualTo("turn-149");
        assertThat(page.items().getLast().correlationId()).isEqualTo("turn-50");
        assertThat(page.nextCursor()).isEqualTo("turn-50");
    }

    @Test
    void get_history_should_use_default_limit_when_limit_is_null() {
        List<ConversationTurn> turns = turns(30);
        given_history(turns);

        ConversationHistoryPage page = service.getHistory(null, null);

        assertThat(page.limit()).isEqualTo(20);
        assertThat(page.items()).hasSize(20);
        assertThat(page.items().getFirst().correlationId()).isEqualTo("turn-29");
        assertThat(page.items().getLast().correlationId()).isEqualTo("turn-10");
        assertThat(page.nextCursor()).isEqualTo("turn-10");
    }

    @Test
    void get_history_should_use_default_limit_when_limit_is_less_than_one() {
        List<ConversationTurn> turns = turns(30);
        given_history(turns);

        ConversationHistoryPage page = service.getHistory(0, null);

        assertThat(page.limit()).isEqualTo(20);
        assertThat(page.items()).hasSize(20);
        assertThat(page.items().getFirst().correlationId()).isEqualTo("turn-29");
        assertThat(page.items().getLast().correlationId()).isEqualTo("turn-10");
    }

    @Test
    void get_history_should_throw_bad_request_when_cursor_is_unknown() {
        List<ConversationTurn> turns = turns(10);
        given_history(turns);

        assertThatThrownBy(() -> service.getHistory(20, "unknown-cursor"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(
                        throwable -> {
                            ResponseStatusException exception = (ResponseStatusException) throwable;
                            assertThat(exception.getStatusCode().value()).isEqualTo(400);
                        });
    }

    private void given_history(List<ConversationTurn> turns) {
        SessionContext sessionContext = org.mockito.Mockito.mock(SessionContext.class);
        sessionStore = org.mockito.Mockito.mock(SessionStore.class);

        when(sessionStore.get()).thenReturn(sessionContext);

        when(sessionContext.getTurns()).thenReturn(turns);

        for (ConversationTurn turn : turns) {
            String correlationId = turn.getCorrelationId();
            ConversationHistoryItem item = item(correlationId);

            when(mapper.toHistoryItem(turn)).thenReturn(item);
        }

        service = new ConversationHistoryService(sessionStore, mapper, clock);
    }

    private List<ConversationTurn> turns(int count) {
        List<ConversationTurn> turns = new ArrayList<>();

        for (int index = 1; index <= count; index++) {
            ConversationTurn turn = org.mockito.Mockito.mock(ConversationTurn.class);
            when(turn.getCorrelationId()).thenReturn("turn-" + index);
            turns.add(turn);
        }

        return turns;
    }

    private ConversationHistoryItem item(String correlationId) {
        return new ConversationHistoryItem(
                correlationId,
                "User text " + correlationId,
                "Assistant text " + correlationId,
                null,
                STARTED_AT,
                COMPLETED_AT);
    }
}
