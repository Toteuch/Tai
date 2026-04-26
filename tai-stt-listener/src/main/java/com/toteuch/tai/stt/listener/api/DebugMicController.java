package com.toteuch.tai.stt.listener.api;

import com.toteuch.tai.stt.listener.api.dto.CaptureDebugResponse;
import com.toteuch.tai.stt.listener.api.dto.GatekeeperDecisionResponse;
import com.toteuch.tai.stt.listener.api.dto.SpeechSegmentResponse;
import com.toteuch.tai.stt.listener.audio.SpeechSegment;
import com.toteuch.tai.stt.listener.capture.MicrophoneCaptureService;
import com.toteuch.tai.stt.listener.gatekeeper.GatekeeperDecision;
import com.toteuch.tai.stt.listener.gatekeeper.TranscriptGatekeeper;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/debug/mic")
public class DebugMicController {
    private final MicrophoneCaptureService captureService;
    private final TranscriptGatekeeper gatekeeper;

    public DebugMicController(MicrophoneCaptureService captureService, TranscriptGatekeeper gatekeeper) {
        this.captureService = captureService;
        this.gatekeeper = gatekeeper;
    }

    @PostMapping("/capture")
    @Operation(summary = "Capture one microphone segment", description = "Captures one microphone segment and runs the pre-gatekeeper decision. No Whisper transcription or orchestrator callback is executed in step 2.")
    public CaptureDebugResponse capture() {
        SpeechSegment segment = captureService.captureOnce();
        GatekeeperDecision preDecision = gatekeeper.preEvaluateSegment(segment);
        return new CaptureDebugResponse(
            true,
            SpeechSegmentResponse.from(segment),
            GatekeeperDecisionResponse.from(preDecision)
        );
    }
}
