package com.toteuch.tai.orchestrator.services.health;

import com.toteuch.tai.orchestrator.system.health.SystemHealthProperties;
import com.toteuch.tai.orchestrator.transport.debug.dto.ServiceHealthResponse;
import com.toteuch.tai.orchestrator.transport.debug.dto.SystemHealthResponse;
import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.stereotype.Service;

@Service
public class SystemHealthAggregator {
    private final SystemHealthProperties properties;
    private final SystemHealthClient healthClient;
    private final ExecutorService executorService =
            Executors.newCachedThreadPool(
                    runnable -> {
                        Thread thread = new Thread(runnable, "tai-system-health-check");
                        thread.setDaemon(true);
                        return thread;
                    });

    public SystemHealthAggregator(
            SystemHealthProperties properties, SystemHealthClient healthClient) {
        this.properties = properties;
        this.healthClient = healthClient;
    }

    public SystemHealthResponse aggregate() {
        Map<String, CompletableFuture<ServiceHealthResponse>> futures = new LinkedHashMap<>();

        properties
                .getServices()
                .forEach(
                        (serviceName, service) ->
                                futures.put(
                                        serviceName,
                                        CompletableFuture.supplyAsync(
                                                () ->
                                                        healthClient.getHealth(
                                                                serviceName, service.getUrl()),
                                                executorService)));

        Map<String, ServiceHealthResponse> services = new LinkedHashMap<>();

        futures.forEach((serviceName, future) -> services.put(serviceName, future.join()));

        return new SystemHealthResponse(computeGlobalStatus(services), Instant.now(), services);
    }

    private String computeGlobalStatus(Map<String, ServiceHealthResponse> services) {
        if (services.isEmpty()) {
            return "UNKNOWN";
        }

        boolean allUp =
                services.values().stream()
                        .allMatch(service -> "UP".equalsIgnoreCase(service.status()));

        return allUp ? "UP" : "DEGRADED";
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdownNow();
    }
}
