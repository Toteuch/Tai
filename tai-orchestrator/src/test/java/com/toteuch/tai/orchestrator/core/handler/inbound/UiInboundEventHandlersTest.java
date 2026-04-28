// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.handler.inbound;

import static org.assertj.core.api.Assertions.assertThat;

import com.toteuch.tai.orchestrator.core.handler.AbstractHandlerTest;
import com.toteuch.tai.orchestrator.core.handler.inbound.ui.UiManualTextInputReceivedEventHandler;
import com.toteuch.tai.orchestrator.core.handler.inbound.ui.UiObscenityFilterToggleChangedEventHandler;
import com.toteuch.tai.orchestrator.core.handler.inbound.ui.UiTtsToggleChangedEventHandler;
import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.inbound.ui.UiManualTextInputReceivedEvent;
import com.toteuch.tai.orchestrator.events.inbound.ui.UiObscenityFilterToggleChangedEvent;
import com.toteuch.tai.orchestrator.events.inbound.ui.UiTtsToggleChangedEvent;
import com.toteuch.tai.orchestrator.events.internal.UserUtteranceAcceptedEvent;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UiInboundEventHandlersTest extends AbstractHandlerTest {

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
}
