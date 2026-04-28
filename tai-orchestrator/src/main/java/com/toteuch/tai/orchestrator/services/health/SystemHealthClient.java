package com.toteuch.tai.orchestrator.services.health;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toteuch.tai.orchestrator.system.health.SystemHealthProperties;
import com.toteuch.tai.orchestrator.transport.debug.dto.ServiceHealthResponse;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SystemHealthClient {
    private static final Logger log = LoggerFactory.getLogger(SystemHealthClient.class);

    private final ObjectMapper objectMapper;
    private final SystemHealthProperties properties;
    private final HttpClient httpClient;

    public SystemHealthClient(ObjectMapper objectMapper, SystemHealthProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                        .version(HttpClient.Version.HTTP_1_1)
                        .build();
    }

    public ServiceHealthResponse getHealth(String serviceName, String url) {
        if (url == null || url.isBlank()) {
            return new ServiceHealthResponse(
                    "UNKNOWN", url, 0L, "Missing health URL for service " + serviceName);
        }

        long startedAt = System.nanoTime();

        try {
            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                            .header("Accept", "application/json")
                            .GET()
                            .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            long responseTimeMs = elapsedMs(startedAt);

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return new ServiceHealthResponse(
                        "DOWN",
                        url,
                        responseTimeMs,
                        "Health endpoint returned HTTP " + response.statusCode());
            }

            String status = extractStatus(response.body());

            return new ServiceHealthResponse(status, url, responseTimeMs, null);
        } catch (Exception e) {
            long responseTimeMs = elapsedMs(startedAt);

            log.debug(
                    "System health check failed | service={} url={} error={}",
                    serviceName,
                    url,
                    e.getMessage());

            return new ServiceHealthResponse(
                    statusFromException(e), url, responseTimeMs, e.getMessage());
        }
    }

    private String extractStatus(String body) {
        if (body == null || body.isBlank()) {
            return "UNKNOWN";
        }

        try {
            JsonNode json = objectMapper.readTree(body);
            JsonNode statusNode = json.get("status");

            if (statusNode == null || statusNode.isNull()) {
                return "UNKNOWN";
            }

            return statusNode.asText("UNKNOWN");
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private String statusFromException(Exception e) {
        String simpleName = e.getClass().getSimpleName();

        if (simpleName.contains("Timeout")) {
            return "TIMEOUT";
        }

        return "DOWN";
    }

    private long elapsedMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000L;
    }
}
