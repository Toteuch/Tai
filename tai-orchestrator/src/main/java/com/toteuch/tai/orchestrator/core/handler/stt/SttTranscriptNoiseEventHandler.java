// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.core.handler.stt;

import com.toteuch.tai.events.EventType;
import com.toteuch.tai.events.stt.SttTranscriptNoiseEvent;
import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SttTranscriptNoiseEventHandler implements EventHandler<SttTranscriptNoiseEvent> {
    private static final Logger decisionLog = LoggerFactory.getLogger("tai.decision");
    private static final Logger perfLog = LoggerFactory.getLogger("tai.performance");

    private final ModuleRuntimeUpdater runtimeUpdater;

    public SttTranscriptNoiseEventHandler(ModuleRuntimeUpdater runtimeUpdater) {
        this.runtimeUpdater = runtimeUpdater;
    }

    @Override
    public EventType supports() {
        return EventType.STT_TRANSCRIPT_NOISE;
    }

    @Override
    public void handle(SttTranscriptNoiseEvent event) {
        perfLog.debug(
                "STT noise received | correlationId={} userSpeechDurationMs={} transcriptionDurationMs={}",
                event.correlationId(),
                event.transcriptionDurationMs(),
                event.speechDurationMs());
        decisionLog.info("STT noise ignored | correlationId={}", event.correlationId());

        runtimeUpdater.sttListenerListening();
    }
}
