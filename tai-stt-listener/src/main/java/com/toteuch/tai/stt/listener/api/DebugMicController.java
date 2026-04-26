package com.toteuch.tai.stt.listener.api;

import com.toteuch.tai.stt.listener.api.dto.CaptureDebugResponse;
import com.toteuch.tai.stt.listener.api.dto.SpeechSegmentResponse;
import com.toteuch.tai.stt.listener.audio.SpeechSegment;
import com.toteuch.tai.stt.listener.capture.MicrophoneCaptureService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/debug/mic")
public class DebugMicController {
    private final MicrophoneCaptureService captureService;

    public DebugMicController(MicrophoneCaptureService captureService) {
        this.captureService = captureService;
    }

    @PostMapping("/capture")
    @Operation(
        summary = "Capture one microphone segment",
        description = "Captures one microphone segment and returns the generated WAV path with audio metrics. No STT/gatekeeper/orchestrator callback is executed in step 1."
    )
    public CaptureDebugResponse capture() {
        SpeechSegment segment = captureService.captureOnce();
        return new CaptureDebugResponse(true, SpeechSegmentResponse.from(segment));
    }
}
