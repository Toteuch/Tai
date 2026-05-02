// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.push;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tai.ui.state-refresh")
public class UiStateRefreshProperties {

    private boolean enabled = true;

    /**
     * Groups several refresh requests into a single projection rebuild.
     */
    private Duration debounce = Duration.ofMillis(25);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Duration getDebounce() {
        return debounce;
    }

    public void setDebounce(Duration debounce) {
        this.debounce = debounce;
    }
}
