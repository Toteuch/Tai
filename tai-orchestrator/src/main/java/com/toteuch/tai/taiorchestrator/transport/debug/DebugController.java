package com.toteuch.tai.taiorchestrator.transport.debug;

import com.toteuch.tai.taiorchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.taiorchestrator.events.EventSource;
import com.toteuch.tai.taiorchestrator.events.inbound.ui.UiManualTextInputReceivedEvent;
import com.toteuch.tai.taiorchestrator.services.stt.audio.capture.MicrophoneCaptureService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/debug")
public class DebugController {

    private final TaiEventPublisher eventPublisher;
    private final MicrophoneCaptureService microphoneCaptureService;

    public DebugController(
        TaiEventPublisher eventPublisher,
        MicrophoneCaptureService microphoneCaptureService
    ) {
        this.eventPublisher = eventPublisher;
        this.microphoneCaptureService = microphoneCaptureService;
    }

    @PostMapping("/text")
    public String sendText(@RequestParam String text) {
        eventPublisher.publish(new UiManualTextInputReceivedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            UUID.randomUUID().toString(),
            EventSource.UI,
            text
        ));
        return "OK";
    }

    @PostMapping("/stt/mic/auto-process/start")
    public String startRecordingAutoProcess(
        @RequestParam(required = false) String correlationId
    ) {
        correlationId = (correlationId == null || correlationId.isBlank())
            ? "test" : correlationId;
        microphoneCaptureService.startRecordingAndWaitForSilence(correlationId);
        return "OK";
    }
}
