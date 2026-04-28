// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.publisher;

import com.toteuch.tai.orchestrator.events.TaiEvent;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class DefaultTaiEventPublisher implements TaiEventPublisher {

    private final ObjectProvider<EventDispatcher> eventDispatcherProvider;

    public DefaultTaiEventPublisher(ObjectProvider<EventDispatcher> eventDispatcherProvider) {
        this.eventDispatcherProvider = eventDispatcherProvider;
    }

    @Override
    public void publish(TaiEvent event) {
        eventDispatcherProvider.getObject().dispatch(event);
    }
}
