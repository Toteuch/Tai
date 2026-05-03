// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.publisher;

import com.toteuch.tai.events.TaiEvent;

public interface TaiEventPublisher {
    void publish(TaiEvent event);
}
