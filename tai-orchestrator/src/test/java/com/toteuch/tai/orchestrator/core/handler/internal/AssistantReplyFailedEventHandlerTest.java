package com.toteuch.tai.orchestrator.core.handler.internal;

import com.toteuch.tai.orchestrator.core.handler.AbstractHandlerTest;
import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.internal.AssistantReplyFailedEvent;
import com.toteuch.tai.orchestrator.events.internal.ConversationTurnCompletedEvent;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.ThinkingState;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AssistantReplyFailedEventHandlerTest extends AbstractHandlerTest {

    @Test
    void should_set_thinking_idle_and_publish_turn_completed() {
        SessionContext context = new SessionContext();
        context.setThinkingState(ThinkingState.GENERATING);

        AssistantReplyFailedEventHandler handler = new AssistantReplyFailedEventHandler(
            fixedSessionStore(context),
            eventPublisher
        );

        handler.handle(new AssistantReplyFailedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            "corr-1",
            EventSource.LLM_SERVICE,
            "LLM_ERROR",
            "LLM failed"
        ));

        assertThat(context.getThinkingState()).isEqualTo(ThinkingState.IDLE);

        ConversationTurnCompletedEvent published =
            eventPublisher.assertSingleEventPublished(ConversationTurnCompletedEvent.class);

        assertThat(published.correlationId()).isEqualTo("corr-1");
    }
}
