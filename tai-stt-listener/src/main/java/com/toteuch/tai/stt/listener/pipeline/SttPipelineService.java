package com.toteuch.tai.stt.listener.pipeline;

import com.toteuch.tai.stt.listener.audio.SpeechSegment;
import com.toteuch.tai.stt.listener.config.SttListenerProperties;
import com.toteuch.tai.stt.listener.gatekeeper.GatekeeperDecision;
import com.toteuch.tai.stt.listener.gatekeeper.TranscriptGatekeeper;
import com.toteuch.tai.stt.listener.transcription.TranscriptionResult;
import com.toteuch.tai.stt.listener.transcription.WhisperTranscriptionClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.time.Instant;

@Service
public class SttPipelineService {
    private static final Logger log = LoggerFactory.getLogger(SttPipelineService.class);

    private final TranscriptGatekeeper gatekeeper;
    private final WhisperTranscriptionClient whisperClient;
    private final SttListenerProperties properties;

    public SttPipelineService(
        TranscriptGatekeeper gatekeeper,
        WhisperTranscriptionClient whisperClient,
        SttListenerProperties properties
    ) {
        this.gatekeeper = gatekeeper;
        this.whisperClient = whisperClient;
        this.properties = properties;
    }

    public SttPipelineResult process(SpeechSegment segment, String correlationId) {
        try {
            GatekeeperDecision preDecision = gatekeeper.preEvaluateSegment(segment);

            if (preDecision != null) {
                return new SttPipelineResult(
                    correlationId,
                    segment,
                    preDecision,
                    null,
                    preDecision,
                    Instant.now()
                );
            }

            TranscriptionResult transcription = whisperClient.transcribe(
                correlationId,
                segment.audioFile()
            );

            GatekeeperDecision finalDecision = gatekeeper.evaluate(segment, transcription);

            return new SttPipelineResult(
                correlationId,
                segment,
                null,
                transcription,
                finalDecision,
                Instant.now()
            );
        } finally {
            if (properties.getListener().isDeleteAudioAfterProcessing()) {
                deleteCapturedWav(correlationId, segment);
            }
        }
    }

    private void deleteCapturedWav(String correlationId, SpeechSegment segment) {
        if (segment == null || segment.audioFile() == null) {
            return;
        }

        try {
            Files.deleteIfExists(segment.audioFile());
            log.debug(
                "Deleted captured WAV | correlationId={} file={}",
                correlationId,
                segment.audioFile().toAbsolutePath()
            );
        } catch (Exception e) {
            log.warn(
                "Failed to delete captured WAV | correlationId={} file={}",
                correlationId,
                segment.audioFile().toAbsolutePath(),
                e
            );
        }
    }
}
