// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.handler.inbound;

import static org.assertj.core.api.Assertions.assertThat;

import com.toteuch.tai.events.EventSource;
import com.toteuch.tai.events.ui.UiManualTextInputReceivedEvent;
import com.toteuch.tai.events.ui.UiObscenityFilterToggleChangedEvent;
import com.toteuch.tai.events.ui.UiStopSpeakReceivedEvent;
import com.toteuch.tai.events.ui.UiTtsToggleChangedEvent;
import com.toteuch.tai.orchestrator.core.handler.AbstractHandlerTest;
import com.toteuch.tai.orchestrator.core.handler.ui.UiManualTextInputReceivedEventHandler;
import com.toteuch.tai.orchestrator.core.handler.ui.UiObscenityFilterToggleChangedEventHandler;
import com.toteuch.tai.orchestrator.core.handler.ui.UiStopSpeakReceivedEventHandler;
import com.toteuch.tai.orchestrator.core.handler.ui.UiTtsToggleChangedEventHandler;
import com.toteuch.tai.orchestrator.events.internal.AssistantStopSpeakReceivedEvent;
import com.toteuch.tai.orchestrator.events.internal.UserUtteranceAcceptedEvent;
import com.toteuch.tai.orchestrator.session.ConversationTurn;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SessionStore;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UiInboundEventHandlersTest extends AbstractHandlerTest {

    private final SessionContext sessionContext = new SessionContext();
    private final SessionStore sessionStore = () -> sessionContext;

    @Test
    void manual_text_input_should_publish_user_utterance_accepted_event() {
        UiManualTextInputReceivedEventHandler handler =
                new UiManualTextInputReceivedEventHandler(eventPublisher);

        handler.handle(
                new UiManualTextInputReceivedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-1",
                        EventSource.UI,
                        "Hello Tai"));

        UserUtteranceAcceptedEvent published =
                eventPublisher.assertSingleEventPublished(UserUtteranceAcceptedEvent.class);

        assertThat(published.correlationId()).isEqualTo("corr-1");
        assertThat(published.source()).isEqualTo(EventSource.UI);
        assertThat(published.text()).isEqualTo("Hello Tai");
    }

    @Test
    void tts_toggle_changed_should_publish_no_event_for_now() {
        UiTtsToggleChangedEventHandler handler = new UiTtsToggleChangedEventHandler();

        handler.handle(
                new UiTtsToggleChangedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-2",
                        EventSource.UI,
                        true));

        eventPublisher.assertNoEventPublished();
    }

    @Test
    void obscenity_filter_toggle_changed_should_publish_no_event_for_now() {
        UiObscenityFilterToggleChangedEventHandler handler =
                new UiObscenityFilterToggleChangedEventHandler();

        handler.handle(
                new UiObscenityFilterToggleChangedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "corr-3",
                        EventSource.UI,
                        true));

        eventPublisher.assertNoEventPublished();
    }

    @Test
    void stop_speak_request_for_active_turn_should_publish_assistant_stop_speak_received_event() {
        sessionContext.setActiveTurn(
                new ConversationTurn("active-corr", "Current input", Instant.now(), true));
        UiStopSpeakReceivedEventHandler handler =
                new UiStopSpeakReceivedEventHandler(sessionStore, eventPublisher);

        handler.handle(
                new UiStopSpeakReceivedEvent(
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        "active-corr",
                        EventSource.UI));
        AssistantStopSpeakReceivedEvent published =
                eventPublisher.assertSingleEventPublished(AssistantStopSpeakReceivedEvent.class);

        assertThat(published.correlationId()).isEqualTo("active-corr");
        assertThat(published.source()).isEqualTo(EventSource.UI);
    }

    @Test
    void stop_speak_for_stale_turn_should_publish_no_event() {
        sessionContext.setActiveTurn(
                new ConversationTurn("active-corr", "Current input", Instant.now(), true));
        UiStopSpeakReceivedEventHandler handler =
                new UiStopSpeakReceivedEventHandler(sessionStore, eventPublisher);

        handler.handle(
                new UiStopSpeakReceivedEvent(
                        UUID.randomUUID().toString(), Instant.now(), "stale-corr", EventSource.UI));

        eventPublisher.assertNoEventPublished();
    }

    @Test
    void stop_speak_for_no_active_turn_should_publish_no_event() {
        UiStopSpeakReceivedEventHandler handler =
                new UiStopSpeakReceivedEventHandler(sessionStore, eventPublisher);

        handler.handle(
                new UiStopSpeakReceivedEvent(
                        UUID.randomUUID().toString(), Instant.now(), "corr-1", EventSource.UI));

        eventPublisher.assertNoEventPublished();
    }
}
