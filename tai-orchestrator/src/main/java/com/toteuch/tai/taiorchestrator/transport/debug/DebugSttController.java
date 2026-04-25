package com.toteuch.tai.taiorchestrator.transport.debug;

import com.toteuch.tai.taiorchestrator.core.UserInputProcessor;
import com.toteuch.tai.taiorchestrator.services.stt.SttClient;
import com.toteuch.tai.taiorchestrator.services.stt.SttResult;
import com.toteuch.tai.taiorchestrator.transport.debug.dto.DebugSttProcessResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.UUID;

@RestController
public class DebugSttController {

    private final SttClient sttClient;
    private final UserInputProcessor userInputProcessor;

    public DebugSttController(
        SttClient sttClient,
        UserInputProcessor userInputProcessor
    ) {
        this.sttClient = sttClient;
        this.userInputProcessor = userInputProcessor;
    }

    @GetMapping("/debug/stt/file")
    public SttResult transcribeFile(
        @RequestParam("path") String path,
        @RequestParam(value = "sessionId", required = false) String sessionId
    ) {
        String effectiveSessionId = (sessionId == null || sessionId.isBlank())
            ? UUID.randomUUID().toString()
            : sessionId;

        return sttClient.transcribe(effectiveSessionId, Path.of(path));
    }

    @GetMapping("/debug/stt/file/process")
    public DebugSttProcessResponse transcribeFileAndProcess(
        @RequestParam("path") String path,
        @RequestParam(value = "sessionId", required = false) String sessionId
    ) {
        String effectiveSessionId = (sessionId == null || sessionId.isBlank())
            ? UUID.randomUUID().toString()
            : sessionId;

        SttResult result = sttClient.transcribe(effectiveSessionId, Path.of(path));

        if (!result.success()) {
            return new DebugSttProcessResponse(
                false,
                null,
                result.language(),
                result.languageProbability(),
                false,
                result.errorCode(),
                result.errorMessage()
            );
        }

        String correlationId = UUID.randomUUID().toString();

        userInputProcessor.processUserText(
            effectiveSessionId,
            correlationId,
            result.text(),
            false
        );

        return new DebugSttProcessResponse(
            true,
            result.text(),
            result.language(),
            result.languageProbability(),
            true,
            null,
            null
        );
    }
}
