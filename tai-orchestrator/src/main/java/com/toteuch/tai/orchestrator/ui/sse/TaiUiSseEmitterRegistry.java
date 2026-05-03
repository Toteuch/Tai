// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.sse;

import com.toteuch.tai.orchestrator.ui.model.TaiUiState;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class TaiUiSseEmitterRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaiUiSseEmitterRegistry.class);

    private final TaiUiSseProperties properties;
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public TaiUiSseEmitterRegistry(TaiUiSseProperties properties) {
        this.properties = properties;
    }

    public SseEmitter register() {
        String emitterId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(properties.getTimeout().toMillis());

        emitters.put(emitterId, emitter);

        emitter.onCompletion(() -> remove(emitterId));
        emitter.onTimeout(() -> remove(emitterId));
        emitter.onError(error -> remove(emitterId));

        LOGGER.debug(
                "Registered Tai UI SSE emitter id={} activeEmitters={}",
                emitterId,
                emitters.size());

        return emitter;
    }

    public void sendInitial(SseEmitter emitter, TaiUiState state) {
        send(emitter, state, "initial");
    }

    public void broadcast(TaiUiState state, Set<String> reasons, Set<String> correlationIds) {
        emitters.forEach(
                (emitterId, emitter) -> {
                    try {
                        emitter.send(
                                SseEmitter.event()
                                        .name("tai-ui-state")
                                        .id(String.valueOf(state.sequence()))
                                        .reconnectTime(properties.getReconnectDelay().toMillis())
                                        .data(state)
                                        .comment(buildComment(reasons, correlationIds)));
                    } catch (IOException | IllegalStateException exception) {
                        LOGGER.debug(
                                "Removing broken Tai UI SSE emitter id={} activeEmitters={}",
                                emitterId,
                                emitters.size(),
                                exception);
                        remove(emitterId);
                    }
                });
    }

    public int activeEmitterCount() {
        return emitters.size();
    }

    private void send(SseEmitter emitter, TaiUiState state, String comment) {
        try {
            emitter.send(
                    SseEmitter.event()
                            .name("tai-ui-state")
                            .id(String.valueOf(state.sequence()))
                            .reconnectTime(properties.getReconnectDelay().toMillis())
                            .data(state)
                            .comment(comment));
        } catch (IOException | IllegalStateException exception) {
            emitter.completeWithError(exception);
        }
    }

    private void remove(String emitterId) {
        SseEmitter removed = emitters.remove(emitterId);

        if (removed != null) {
            LOGGER.debug(
                    "Removed Tai UI SSE emitter id={} activeEmitters={}",
                    emitterId,
                    emitters.size());
        }
    }

    private String buildComment(Set<String> reasons, Set<String> correlationIds) {
        return "reasons=" + reasons + " correlationIds=" + correlationIds;
    }
}
