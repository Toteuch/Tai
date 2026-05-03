// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.health;

import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class HttpModuleHealthClient implements ModuleHealthClient {

    private static final String STATUS_UP = "UP";
    private static final String STATUS_DOWN = "DOWN";
    private static final String STATUS_DEGRADED = "DEGRADED";

    private final ModuleHealthRefreshProperties properties;
    private final RestTemplate restTemplate;
    private final Clock clock;

    public HttpModuleHealthClient(
            ModuleHealthRefreshProperties properties,
            @Qualifier("moduleHealthRestTemplate") RestTemplate restTemplate,
            Clock clock) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.clock = clock;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ModuleHealthResult check(TaiModule module) {
        URI endpoint = properties.endpoint(module);

        if (endpoint == null) {
            return ModuleHealthResult.failure(
                    module,
                    STATUS_DEGRADED,
                    clock.instant(),
                    "No health endpoint configured for module " + module);
        }

        try {
            Map<String, Object> body = restTemplate.getForObject(endpoint, Map.class);
            Instant respondedAt = clock.instant();

            if (body == null) {
                return ModuleHealthResult.failure(
                        module,
                        STATUS_DEGRADED,
                        respondedAt,
                        "Health endpoint returned an empty body");
            }

            return ModuleHealthResult.success(module, readStatus(body), respondedAt, body, null);
        } catch (RestClientException exception) {
            return ModuleHealthResult.failure(
                    module, STATUS_DOWN, clock.instant(), exception.getMessage());
        }
    }

    private String readStatus(Map<String, Object> body) {
        Object status = body.get("status");

        if (status == null) {
            return STATUS_DEGRADED;
        }

        return mapActuatorStatus(status.toString());
    }

    private String mapActuatorStatus(String status) {
        return switch (status) {
            case "UP" -> STATUS_UP;
            case "DOWN" -> STATUS_DOWN;
            case "DEGRADED" -> STATUS_DEGRADED;
            case "OUT_OF_SERVICE" -> STATUS_DEGRADED;
            case "UNKNOWN" -> STATUS_DEGRADED;
            default -> STATUS_DEGRADED;
        };
    }
}
