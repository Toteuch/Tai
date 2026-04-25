package com.toteuch.tai.taiorchestrator.core.publisher;

import com.toteuch.tai.taiorchestrator.events.TaiEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
