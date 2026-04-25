package com.toteuch.tai.taiorchestrator.core.handler;

import com.toteuch.tai.taiorchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.taiorchestrator.events.TaiEvent;
import com.toteuch.tai.taiorchestrator.session.SessionContext;
import com.toteuch.tai.taiorchestrator.session.SessionStore;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractHandlerTest {

    protected final CapturingTaiEventPublisher eventPublisher = new CapturingTaiEventPublisher();

    protected SessionStore fixedSessionStore(SessionContext sessionContext) {
        return () -> sessionContext;
    }

    protected static class CapturingTaiEventPublisher implements TaiEventPublisher {

        private final List<TaiEvent> publishedEvents = new ArrayList<>();

        @Override
        public void publish(TaiEvent event) {
            publishedEvents.add(event);
        }

        public List<TaiEvent> publishedEvents() {
            return publishedEvents;
        }

        public void assertNoEventPublished() {
            assertThat(publishedEvents).isEmpty();
        }

        public <T extends TaiEvent> T assertSingleEventPublished(Class<T> expectedType) {
            assertThat(publishedEvents).hasSize(1);
            assertThat(publishedEvents.getFirst()).isInstanceOf(expectedType);
            return expectedType.cast(publishedEvents.getFirst());
        }
    }
}
