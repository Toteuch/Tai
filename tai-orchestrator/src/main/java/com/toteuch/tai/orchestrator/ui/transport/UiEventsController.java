// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.transport;

import com.toteuch.tai.orchestrator.ui.model.TaiUiState;
import com.toteuch.tai.orchestrator.ui.projection.TaiUiStateProjectionService;
import com.toteuch.tai.orchestrator.ui.sse.TaiUiSseEmitterRegistry;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/ui/events")
public class UiEventsController {

    private final TaiUiSseEmitterRegistry emitterRegistry;
    private final TaiUiStateProjectionService projectionService;

    public UiEventsController(
            TaiUiSseEmitterRegistry emitterRegistry,
            TaiUiStateProjectionService projectionService) {
        this.emitterRegistry = emitterRegistry;
        this.projectionService = projectionService;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events() {
        SseEmitter emitter = emitterRegistry.register();

        TaiUiState initialState = projectionService.currentOrRebuild();
        emitterRegistry.sendInitial(emitter, initialState);

        return emitter;
    }
}
