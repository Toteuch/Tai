// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.publisher;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.TaiEvent;
import java.util.List;
import org.junit.jupiter.api.Test;

class EventDispatcherTest {

    @Test
    void should_dispatch_event_to_correct_handler() {
        EventHandler<TaiEvent> handler = mock(EventHandler.class);
        when(handler.supports()).thenReturn(EventType.USER_UTTERANCE_ACCEPTED);

        EventDispatcher dispatcher = new EventDispatcher(List.of(handler));
        dispatcher.init();

        TaiEvent event = mock(TaiEvent.class);
        when(event.type()).thenReturn(EventType.USER_UTTERANCE_ACCEPTED);
        when(event.correlationId()).thenReturn("corr-1");

        dispatcher.dispatch(event);

        verify(handler).handle(event);
    }

    @Test
    void should_throw_when_no_handler_found() {
        EventDispatcher dispatcher = new EventDispatcher(List.of());
        dispatcher.init();

        TaiEvent event = mock(TaiEvent.class);
        when(event.type()).thenReturn(EventType.USER_UTTERANCE_ACCEPTED);

        assertThatThrownBy(() -> dispatcher.dispatch(event))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No handler registered");
    }

    @Test
    void should_use_last_handler_if_duplicate_event_type() {
        EventHandler<TaiEvent> handler1 = mock(EventHandler.class);
        EventHandler<TaiEvent> handler2 = mock(EventHandler.class);

        when(handler1.supports()).thenReturn(EventType.USER_UTTERANCE_ACCEPTED);
        when(handler2.supports()).thenReturn(EventType.USER_UTTERANCE_ACCEPTED);

        EventDispatcher dispatcher = new EventDispatcher(List.of(handler1, handler2));
        dispatcher.init();

        TaiEvent event = mock(TaiEvent.class);
        when(event.type()).thenReturn(EventType.USER_UTTERANCE_ACCEPTED);
        when(event.correlationId()).thenReturn("corr-1");

        dispatcher.dispatch(event);

        verify(handler2).handle(event);
        verify(handler1, never()).handle(event);
    }
}
