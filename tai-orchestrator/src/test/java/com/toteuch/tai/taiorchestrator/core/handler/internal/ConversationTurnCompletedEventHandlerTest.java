package com.toteuch.tai.taiorchestrator.core.handler.internal;

import com.toteuch.tai.taiorchestrator.core.handler.AbstractHandlerTest;
import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.internal.ConversationTurnCompletedEvent;
import com.toteuch.tai.taiorchestrator.session.ConversationTurn;
import com.toteuch.tai.taiorchestrator.session.SessionContext;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationTurnCompletedEventHandlerTest extends AbstractHandlerTest {

    @Test
    void should_persist_active_turn_when_persist_in_history_is_true() {
        SessionContext context = new SessionContext();
        ConversationTurn turn = new ConversationTurn("corr-1", "Hello", Instant.now(), true);
        context.setActiveTurn(turn);

        ConversationTurnCompletedEventHandler handler =
            new ConversationTurnCompletedEventHandler(fixedSessionStore(context));

        handler.handle(new ConversationTurnCompletedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            "corr-1",
            EventSource.ORCHESTRATOR
        ));

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

        handler.handle(new ConversationTurnCompletedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            "corr-1",
            EventSource.ORCHESTRATOR
        ));

        assertThat(context.getTurns()).isEmpty();
        assertThat(context.getActiveTurn()).isNull();
    }
}
