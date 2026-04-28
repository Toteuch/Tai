// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.stt.listener.api;

import com.toteuch.tai.stt.listener.api.dto.CaptureDebugResponse;
import com.toteuch.tai.stt.listener.api.dto.GatekeeperDecisionResponse;
import com.toteuch.tai.stt.listener.api.dto.SpeechSegmentResponse;
import com.toteuch.tai.stt.listener.api.dto.TranscriptionResponse;
import com.toteuch.tai.stt.listener.audio.SpeechSegment;
import com.toteuch.tai.stt.listener.capture.MicrophoneCaptureService;
import com.toteuch.tai.stt.listener.pipeline.SttPipelineResult;
import com.toteuch.tai.stt.listener.pipeline.SttPipelineService;
import com.toteuch.tai.stt.listener.transport.OrchestratorSttEventClient;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debug/mic")
public class DebugMicController {
    private static final Logger log = LoggerFactory.getLogger(DebugMicController.class);

    private final MicrophoneCaptureService captureService;
    private final SttPipelineService pipelineService;
    private final OrchestratorSttEventClient eventClient;

    public DebugMicController(
            MicrophoneCaptureService captureService,
            SttPipelineService pipelineService,
            OrchestratorSttEventClient eventClient) {
        this.captureService = captureService;
        this.pipelineService = pipelineService;
        this.eventClient = eventClient;
    }

    @PostMapping("/capture")
    @Operation(
            summary = "Capture, pre-filter, transcribe and post-filter one microphone segment",
            description = "Runs the full debug STT listener pipeline without sending callbacks.")
    public CaptureDebugResponse capture(@RequestParam(required = false) String correlationId) {
        String effectiveCorrelationId = resolveCorrelationId(correlationId);

        SpeechSegment segment = captureService.captureOnce();
        SttPipelineResult result = pipelineService.process(segment, effectiveCorrelationId);

        return toResponse(result);
    }

    @PostMapping("/capture-and-callback")
    @Operation(
            summary = "Capture, transcribe, filter and callback orchestrator",
            description =
                    "Runs the full STT listener pipeline and sends the resulting callback to the orchestrator.")
    public CaptureDebugResponse captureAndCallback(
            @RequestParam(required = false) String correlationId) {
        String effectiveCorrelationId = resolveCorrelationId(correlationId);

        SpeechSegment segment = captureService.captureOnce();
        SttPipelineResult result = pipelineService.process(segment, effectiveCorrelationId);

        eventClient.sendCallback(
                result.correlationId(),
                result.segment(),
                result.transcription(),
                result.finalDecision());

        return toResponse(result);
    }

    private String resolveCorrelationId(String correlationId) {
        return correlationId == null || correlationId.isBlank()
                ? UUID.randomUUID().toString()
                : correlationId;
    }

    private CaptureDebugResponse toResponse(SttPipelineResult result) {
        return new CaptureDebugResponse(
                true,
                result.correlationId(),
                SpeechSegmentResponse.from(result.segment()),
                GatekeeperDecisionResponse.from(result.preDecision()),
                TranscriptionResponse.from(result.transcription()),
                GatekeeperDecisionResponse.from(result.finalDecision()));
    }
}
