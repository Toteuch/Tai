// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core;

import com.toteuch.tai.events.EventType;
import com.toteuch.tai.events.TaiEvent;

public interface EventHandler<T extends TaiEvent> {
    EventType supports();

    void handle(T event);
}
