// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.handler.inbound;

import static org.assertj.core.api.Assertions.assertThat;

import com.toteuch.tai.events.EventSource;
import com.toteuch.tai.events.llm.LlmResponseCompletedEvent;
import com.toteuch.tai.events.llm.LlmResponseFailedEvent;
import com.toteuch.tai.orchestrator.core.handler.AbstractHandlerTest;
import com.toteuch.tai.orchestrator.core.handler.llm.LlmResponseCompletedEventHandler;
import com.toteuch.tai.orchestrator.core.handler.llm.LlmResponseFailedEventHandler;
import com.toteuch.tai.orchestrator.events.internal.AssistantReplyAcceptedEvent;
import com.toteuch.tai.orchestrator.events.internal.AssistantReplyFailedEvent;
import com.toteuch.tai.orchestrator.session.ConversationTurn;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SessionStore;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LlmInboundEventHandlersTest extends AbstractHandlerTest {

    private final SessionContext sessionContext = new SessionContext();

    private final SessionStore sessionStore = () -> sessionContext;

    @Test
    void completed_response_for_active_turn_should_publish_assistant_reply_accepted_event() {
        sessionContext.setActiveTurn(
                new ConversationTurn("corr-1", "Hello Tai", Instant.now(), true));

        LlmResponseCompletedEventHandler handler =
                new LlmResponseCompletedEventHandler(sessionStore, eventPublisher);

        handler.handle(
                new LlmResponseCompletedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-1",
                        EventSource.LLM_SERVICE,
                        "Hi!",
                        "tai-llama",
                        10,
                        20,
                        250L));

        AssistantReplyAcceptedEvent published =
                eventPublisher.assertSingleEventPublished(AssistantReplyAcceptedEvent.class);

        assertThat(published.correlationId()).isEqualTo("corr-1");
        assertThat(published.source()).isEqualTo(EventSource.LLM_SERVICE);
        assertThat(published.replyText()).isEqualTo("Hi!");
    }

    @Test
    void completed_response_for_stale_turn_should_publish_no_event() {
        sessionContext.setActiveTurn(
                new ConversationTurn("active-corr", "Current input", Instant.now(), true));

        LlmResponseCompletedEventHandler handler =
                new LlmResponseCompletedEventHandler(sessionStore, eventPublisher);

        handler.handle(
                new LlmResponseCompletedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "stale-corr",
                        EventSource.LLM_SERVICE,
                        "Late reply",
                        "tai-llama",
                        10,
                        20,
                        250L));

        eventPublisher.assertNoEventPublished();
    }

    @Test
    void failed_response_for_active_turn_should_publish_assistant_reply_failed_event() {
        sessionContext.setActiveTurn(
                new ConversationTurn("corr-2", "Hello Tai", Instant.now(), true));

        LlmResponseFailedEventHandler handler =
                new LlmResponseFailedEventHandler(sessionStore, eventPublisher);

        handler.handle(
                new LlmResponseFailedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-2",
                        EventSource.LLM_SERVICE,
                        MODEL_NAME,
                        0L,
                        "LLM_ERROR",
                        "LLM failed"));

        AssistantReplyFailedEvent published =
                eventPublisher.assertSingleEventPublished(AssistantReplyFailedEvent.class);

        assertThat(published.correlationId()).isEqualTo("corr-2");
        assertThat(published.source()).isEqualTo(EventSource.LLM_SERVICE);
        assertThat(published.errorCode()).isEqualTo("LLM_ERROR");
        assertThat(published.errorMessage()).isEqualTo("LLM failed");
    }

    @Test
    void failed_response_for_stale_turn_should_publish_no_event() {
        sessionContext.setActiveTurn(
                new ConversationTurn("active-corr", "Current input", Instant.now(), true));

        LlmResponseFailedEventHandler handler =
                new LlmResponseFailedEventHandler(sessionStore, eventPublisher);

        handler.handle(
                new LlmResponseFailedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "stale-corr",
                        EventSource.LLM_SERVICE,
                        MODEL_NAME,
                        0L,
                        "LLM_ERROR",
                        "LLM failed"));

        eventPublisher.assertNoEventPublished();
    }
}
