// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.health;

import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import java.net.URI;
import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tai.ui.health-refresh")
public class ModuleHealthRefreshProperties {

    /**
     * Whether the health refresh is enabled.
     */
    private boolean enabled = true;

    /**
     * How often the scheduler checks whether modules need a health refresh.
     */
    private Duration interval = Duration.ofSeconds(5);

    /**
     * Max age of lastHealthAt before a module is considered health-stale.
     */
    private Duration freshnessThreshold = Duration.ofSeconds(15);

    /**
     * HTTP timeout for a module health call.
     */
    private Duration requestTimeout = Duration.ofSeconds(2);

    /**
     * Minimum delay between two ERROR-triggered refresh attempts for the same module.
     */
    private Duration errorRefreshMinDelay = Duration.ofSeconds(3);

    /**
     * Minimum delay between two refresh attempts for the same module, regardless of reason.
     */
    private Duration minDelayBetweenAttempts = Duration.ofSeconds(1);

    /**
     * Minimum delay between two UI push.
     */
    private Duration uiPushDebounce = Duration.ofMillis(75);

    /**
     * Health endpoints by module.
     *
     * Example:
     *
     * tai:
     *   ui:
     *     health-refresh:
     *       endpoints:
     *         ORCHESTRATOR: http://localhost:8080/actuator/health
     *         STT_LISTENER: http://localhost:8094/actuator/health
     *         STT_WHISPER: http://localhost:8095/health
     *         LLM: http://localhost:8092/actuator/health
     *         TTS_PIPER: http://localhost:8093/actuator/health
     */
    private Map<TaiModule, URI> endpoints = new EnumMap<>(TaiModule.class);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Duration getInterval() {
        return interval;
    }

    public void setInterval(Duration interval) {
        this.interval = interval;
    }

    public Duration getFreshnessThreshold() {
        return freshnessThreshold;
    }

    public void setFreshnessThreshold(Duration freshnessThreshold) {
        this.freshnessThreshold = freshnessThreshold;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public Duration getErrorRefreshMinDelay() {
        return errorRefreshMinDelay;
    }

    public void setErrorRefreshMinDelay(Duration errorRefreshMinDelay) {
        this.errorRefreshMinDelay = errorRefreshMinDelay;
    }

    public Duration getMinDelayBetweenAttempts() {
        return minDelayBetweenAttempts;
    }

    public void setMinDelayBetweenAttempts(Duration minDelayBetweenAttempts) {
        this.minDelayBetweenAttempts = minDelayBetweenAttempts;
    }

    public Map<TaiModule, URI> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Map<TaiModule, URI> endpoints) {
        this.endpoints =
                endpoints != null ? new EnumMap<>(endpoints) : new EnumMap<>(TaiModule.class);
    }

    public boolean hasEndpoint(TaiModule module) {
        return endpoints.containsKey(module);
    }

    public URI endpoint(TaiModule module) {
        return endpoints.get(module);
    }

    public Duration getUiPushDebounce() {
        return uiPushDebounce;
    }

    public void setUiPushDebounce(Duration uiPushDebounce) {
        this.uiPushDebounce = uiPushDebounce;
    }
}
