package com.toteuch.tai.stt.listener.api;

import com.toteuch.tai.stt.listener.api.dto.CaptureDebugResponse;
import com.toteuch.tai.stt.listener.api.dto.GatekeeperDecisionResponse;
import com.toteuch.tai.stt.listener.api.dto.SpeechSegmentResponse;
import com.toteuch.tai.stt.listener.api.dto.TranscriptionResponse;
import com.toteuch.tai.stt.listener.audio.SpeechSegment;
import com.toteuch.tai.stt.listener.capture.MicrophoneCaptureService;
import com.toteuch.tai.stt.listener.gatekeeper.GatekeeperDecision;
import com.toteuch.tai.stt.listener.gatekeeper.TranscriptGatekeeper;
import com.toteuch.tai.stt.listener.transcription.TranscriptionResult;
import com.toteuch.tai.stt.listener.transcription.WhisperTranscriptionClient;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/debug/mic")
public class DebugMicController {
    private final MicrophoneCaptureService captureService;
    private final TranscriptGatekeeper gatekeeper;
    private final WhisperTranscriptionClient whisperClient;

    public DebugMicController(
        MicrophoneCaptureService captureService,
        TranscriptGatekeeper gatekeeper,
        WhisperTranscriptionClient whisperClient
    ) {
        this.captureService = captureService;
        this.gatekeeper = gatekeeper;
        this.whisperClient = whisperClient;
    }

    @PostMapping("/capture")
    @Operation(
        summary = "Capture, pre-filter, transcribe and post-filter one microphone segment",
        description = "Runs the full debug STT listener pipeline: microphone capture, pre-gatekeeper, Whisper transcription when allowed, and final gatekeeper decision."
    )
    public CaptureDebugResponse capture(
        @RequestParam(required = false) String correlationId
    ) {
        String effectiveCorrelationId = correlationId == null || correlationId.isBlank()
            ? UUID.randomUUID().toString()
            : correlationId;

        SpeechSegment segment = captureService.captureOnce();

        GatekeeperDecision preDecision = gatekeeper.preEvaluateSegment(segment);

        if (preDecision != null) {
            return new CaptureDebugResponse(
                true,
                effectiveCorrelationId,
                SpeechSegmentResponse.from(segment),
                GatekeeperDecisionResponse.from(preDecision),
                null,
                GatekeeperDecisionResponse.from(preDecision)
            );
        }

        TranscriptionResult transcription = whisperClient.transcribe(effectiveCorrelationId, segment.audioFile());
        GatekeeperDecision finalDecision = gatekeeper.evaluate(segment, transcription);

        return new CaptureDebugResponse(
            true,
            effectiveCorrelationId,
            SpeechSegmentResponse.from(segment),
            null,
            TranscriptionResponse.from(transcription),
            GatekeeperDecisionResponse.from(finalDecision)
        );
    }
}
