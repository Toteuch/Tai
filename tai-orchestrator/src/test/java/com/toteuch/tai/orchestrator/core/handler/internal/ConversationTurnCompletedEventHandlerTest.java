package com.toteuch.tai.orchestrator.core.handler.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.toteuch.tai.orchestrator.core.handler.AbstractHandlerTest;
import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.internal.ConversationTurnCompletedEvent;
import com.toteuch.tai.orchestrator.session.ConversationTurn;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.TurnMetricsOutcome;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ConversationTurnCompletedEventHandlerTest extends AbstractHandlerTest {

    @Test
    void should_persist_active_turn_when_persist_in_history_is_true() {
        SessionContext context = new SessionContext();
        ConversationTurn turn = new ConversationTurn("corr-1", "Hello", Instant.now(), true);
        context.setActiveTurn(turn);

        ConversationTurnCompletedEventHandler handler =
                new ConversationTurnCompletedEventHandler(fixedSessionStore(context));

        handler.handle(
                new ConversationTurnCompletedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-1",
                        EventSource.ORCHESTRATOR,
                        TurnMetricsOutcome.COMPLETED));

        assertThat(context.getTurns()).containsExactly(turn);
        assertThat(context.getActiveTurn()).isNull();
    }

    @Test
    void should_not_persist_active_turn_when_persist_in_history_is_false() {
        SessionContext context = new SessionContext();
        ConversationTurn turn = new ConversationTurn("corr-1", "...", Instant.now(), false);
        context.setActiveTurn(turn);

        ConversationTurnCompletedEventHandler handler =
                new ConversationTurnCompletedEventHandler(fixedSessionStore(context));

        handler.handle(
                new ConversationTurnCompletedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-1",
                        EventSource.ORCHESTRATOR,
                        TurnMetricsOutcome.COMPLETED));

        assertThat(context.getTurns()).isEmpty();
        assertThat(context.getActiveTurn()).isNull();
    }
}
