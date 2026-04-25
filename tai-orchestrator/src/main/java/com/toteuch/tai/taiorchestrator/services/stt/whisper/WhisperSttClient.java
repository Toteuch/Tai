package com.toteuch.tai.taiorchestrator.services.stt.whisper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toteuch.tai.taiorchestrator.services.stt.SttClient;
import com.toteuch.tai.taiorchestrator.services.stt.SttResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

@Component
@Primary
public class WhisperSttClient implements SttClient {

    private static final Logger log = LoggerFactory.getLogger(WhisperSttClient.class);

    private final WhisperSttProperties properties;
    private final ObjectMapper objectMapper;

    public WhisperSttClient(
        WhisperSttProperties properties,
        ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public SttResult transcribe(String sessionId, Path audioFile) {
        try {
            if (audioFile == null || !Files.exists(audioFile)) {
                return new SttResult(
                    false,
                    null,
                    null,
                    null,
                    "STT_FILE_NOT_FOUND",
                    "Audio file does not exist: " + audioFile
                );
            }

            ProcessBuilder processBuilder = new ProcessBuilder(
                properties.getPythonExecutable(),
                properties.getScriptPath(),
                audioFile.toString(),
                properties.getModelSize(),
                properties.getDevice(),
                properties.getComputeType()
            );

            processBuilder.redirectErrorStream(true);

            log.debug("Calling Whisper STT | sessionId={} audioFile={} model={} device={} computeType={}",
                sessionId,
                audioFile,
                properties.getModelSize(),
                properties.getDevice(),
                properties.getComputeType());

            Process process = processBuilder.start();

            String output;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                output = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }

            int exitCode = process.waitFor();

            log.debug("Whisper STT finished | sessionId={} exitCode={} rawOutput={}",
                sessionId,
                exitCode,
                output);

            if (exitCode != 0) {
                return new SttResult(
                    false,
                    null,
                    null,
                    null,
                    "WHISPER_PROCESS_ERROR",
                    output
                );
            }

            JsonNode root = objectMapper.readTree(output);

            if (!root.path("success").asBoolean(false)) {
                return new SttResult(
                    false,
                    null,
                    null,
                    null,
                    "WHISPER_TRANSCRIPTION_FAILED",
                    root.path("error").asText("Unknown transcription error")
                );
            }

            String text = normalizeTaiName(root.path("text").asText(null));

            return new SttResult(
                true,
                text,
                root.path("language").asText(null),
                root.path("language_probability").isNumber() ? root.path("language_probability").asDouble() : null,
                null,
                null
            );

        } catch (Exception e) {
            log.error("Whisper STT failed | sessionId={} audioFile={}", sessionId, audioFile, e);

            return new SttResult(
                false,
                null,
                null,
                null,
                "WHISPER_UNEXPECTED_ERROR",
                e.getMessage()
            );
        }
    }

    private String normalizeTaiName(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }

        return text
            .replace("Ty", "Tai")
            .replace("Thaï", "Tai")
            .replace("Thai", "Tai");
    }
}
