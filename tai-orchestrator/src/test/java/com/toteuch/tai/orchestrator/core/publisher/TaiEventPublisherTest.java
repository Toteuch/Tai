// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.publisher;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.toteuch.tai.orchestrator.events.TaiEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

class TaiEventPublisherTest {

    @Test
    void should_delegate_publish_to_event_dispatcher() {
        EventDispatcher dispatcher = mock(EventDispatcher.class);
        ObjectProvider<EventDispatcher> provider = mock(ObjectProvider.class);

        when(provider.getObject()).thenReturn(dispatcher);

        DefaultTaiEventPublisher publisher = new DefaultTaiEventPublisher(provider);

        TaiEvent event = mock(TaiEvent.class);

        publisher.publish(event);

        verify(dispatcher).dispatch(event);
    }

    @Test
    void should_call_provider_each_time_publish_is_called() {
        EventDispatcher dispatcher = mock(EventDispatcher.class);
        ObjectProvider<EventDispatcher> provider = mock(ObjectProvider.class);

        when(provider.getObject()).thenReturn(dispatcher);

        DefaultTaiEventPublisher publisher = new DefaultTaiEventPublisher(provider);

        publisher.publish(mock(TaiEvent.class));
        publisher.publish(mock(TaiEvent.class));

        verify(provider, times(2)).getObject();
    }
}
