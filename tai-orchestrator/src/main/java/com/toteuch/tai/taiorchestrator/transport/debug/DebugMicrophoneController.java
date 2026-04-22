package com.toteuch.tai.taiorchestrator.transport.debug;

import com.toteuch.tai.taiorchestrator.core.UserInputProcessor;
import com.toteuch.tai.taiorchestrator.services.audio.capture.MicrophoneCaptureService;
import com.toteuch.tai.taiorchestrator.services.audio.capture.MicrophoneStopResult;
import com.toteuch.tai.taiorchestrator.services.stt.SttClient;
import com.toteuch.tai.taiorchestrator.services.stt.SttResult;
import com.toteuch.tai.taiorchestrator.transport.debug.dto.DebugMicResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.UUID;

@RestController
public class DebugMicrophoneController {

    private final MicrophoneCaptureService microphoneCaptureService;
    private final SttClient sttClient;
    private final UserInputProcessor userInputProcessor;

    public DebugMicrophoneController(
        MicrophoneCaptureService microphoneCaptureService,
        SttClient sttClient,
        UserInputProcessor userInputProcessor
    ) {
        this.microphoneCaptureService = microphoneCaptureService;
        this.sttClient = sttClient;
        this.userInputProcessor = userInputProcessor;
    }

    @PostMapping("/debug/stt/mic/start")
    public DebugMicResponse startRecording(
        @RequestParam(value = "sessionId", required = false) String sessionId
    ) throws Exception {
        String effectiveSessionId = (sessionId == null || sessionId.isBlank())
            ? UUID.randomUUID().toString()
            : sessionId;

        Path audioFile = microphoneCaptureService.startRecording(effectiveSessionId);

        return new DebugMicResponse(
            true,
            effectiveSessionId,
            true,
            audioFile.toString(),
            null,
            null,
            null,
            false,
            null,
            null
        );
    }

    @PostMapping("/debug/stt/mic/stop")
    public DebugMicResponse stopRecording(
        @RequestParam(value = "sessionId", required = false) String sessionId
    ) {
        String effectiveSessionId = (sessionId == null || sessionId.isBlank())
            ? "test"
            : sessionId;

        MicrophoneStopResult stopResult = microphoneCaptureService.stopRecording(effectiveSessionId);

        return new DebugMicResponse(
            stopResult.success(),
            effectiveSessionId,
            false,
            stopResult.audioFile() != null ? stopResult.audioFile().toString() : null,
            null,
            null,
            null,
            false,
            stopResult.errorCode(),
            stopResult.errorMessage()
        );
    }

    @PostMapping("/debug/stt/mic/stop-and-process")
    public DebugMicResponse stopRecordingAndProcess(
        @RequestParam(value = "sessionId", required = false) String sessionId
    ) {
        String effectiveSessionId = (sessionId == null || sessionId.isBlank())
            ? "test"
            : sessionId;

        MicrophoneStopResult stopResult = microphoneCaptureService.stopRecording(effectiveSessionId);
        if (!stopResult.success()) {
            return new DebugMicResponse(
                false,
                effectiveSessionId,
                false,
                null,
                null,
                null,
                null,
                false,
                stopResult.errorCode(),
                stopResult.errorMessage()
            );
        }

        SttResult sttResult = sttClient.transcribe(effectiveSessionId, stopResult.audioFile());
        if (!sttResult.success()) {
            return new DebugMicResponse(
                false,
                effectiveSessionId,
                false,
                stopResult.audioFile().toString(),
                null,
                sttResult.language(),
                sttResult.languageProbability(),
                false,
                sttResult.errorCode(),
                sttResult.errorMessage()
            );
        }

        String correlationId = UUID.randomUUID().toString();

        userInputProcessor.processUserText(
            effectiveSessionId,
            correlationId,
            sttResult.text(),
            false
        );

        return new DebugMicResponse(
            true,
            effectiveSessionId,
            false,
            stopResult.audioFile().toString(),
            sttResult.text(),
            sttResult.language(),
            sttResult.languageProbability(),
            true,
            null,
            null
        );
    }

    @PostMapping("/debug/stt/mic/auto-process/start")
    public DebugMicResponse startRecordingAutoProcess(
        @RequestParam(value = "sessionId", required = false) String sessionId
    ) {
        String effectiveSessionId = (sessionId == null || sessionId.isBlank())
            ? "test"
            : sessionId;

        MicrophoneStopResult stopResult = microphoneCaptureService.startRecordingAndWaitForSilence(effectiveSessionId);
        if (!stopResult.success()) {
            return new DebugMicResponse(
                false,
                effectiveSessionId,
                false,
                null,
                null,
                null,
                null,
                false,
                stopResult.errorCode(),
                stopResult.errorMessage()
            );
        }

        SttResult sttResult = sttClient.transcribe(effectiveSessionId, stopResult.audioFile());
        if (!sttResult.success()) {
            return new DebugMicResponse(
                false,
                effectiveSessionId,
                false,
                stopResult.audioFile().toString(),
                null,
                sttResult.language(),
                sttResult.languageProbability(),
                false,
                sttResult.errorCode(),
                sttResult.errorMessage()
            );
        }

        String correlationId = UUID.randomUUID().toString();

        userInputProcessor.processUserText(
            effectiveSessionId,
            correlationId,
            sttResult.text(),
            false
        );

        return new DebugMicResponse(
            true,
            effectiveSessionId,
            false,
            stopResult.audioFile().toString(),
            sttResult.text(),
            sttResult.language(),
            sttResult.languageProbability(),
            true,
            null,
            null
        );
    }
}
