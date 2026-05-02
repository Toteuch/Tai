// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.sse;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tai.ui.sse")
public class TaiUiSseProperties {

    /**
     * SSE emitter timeout.
     */
    private Duration timeout = Duration.ofHours(1);

    /**
     * Browser reconnect delay advertised through SSE events.
     */
    private Duration reconnectDelay = Duration.ofSeconds(2);

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public Duration getReconnectDelay() {
        return reconnectDelay;
    }

    public void setReconnectDelay(Duration reconnectDelay) {
        this.reconnectDelay = reconnectDelay;
    }
}
