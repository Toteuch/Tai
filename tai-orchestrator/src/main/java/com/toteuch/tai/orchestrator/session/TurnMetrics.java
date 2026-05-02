// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.session;

import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TurnMetrics {
    private static final Logger perfLog = LoggerFactory.getLogger("tai.performance");
    private static final Logger errorLog = LoggerFactory.getLogger("tai.error");

    private final String correlationId;
    Instant userSpeechStartAt;
    Instant userUtteranceAcceptedAt;
    Instant ttsSpeechStartAt;
    Long userSpeechDurationMs;
    Long speechToTranscriptMs;
    Long transcriptDurationMs;
    Long llmGenerationMs;
    Long ttsSynthesisMs;
    Long ttsSpeechDurationMs;

    public TurnMetrics(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setUserSpeechStartAt(Instant userSpeechStartAt) {
        this.userSpeechStartAt =
                safeSet("userSpeechStartAt", this.userSpeechStartAt, userSpeechStartAt);
    }

    public void setUserUtteranceAcceptedAt(Instant userUtteranceAcceptedAt) {
        this.userUtteranceAcceptedAt =
                safeSet(
                        "userUtteranceAcceptedAt",
                        this.userUtteranceAcceptedAt,
                        userUtteranceAcceptedAt);

        if (userSpeechStartAt != null) {
            this.speechToTranscriptMs =
                    safeSet(
                            "speechToTranscriptMs",
                            this.speechToTranscriptMs,
                            Duration.between(userSpeechStartAt, userUtteranceAcceptedAt)
                                    .toMillis());
        }
    }

    public void setTtsSpeechStartAt(Instant ttsSpeechStartAt) {
        this.ttsSpeechStartAt =
                safeSet("ttsSpeechStartAt", this.ttsSpeechStartAt, ttsSpeechStartAt);
    }

    public void setTranscriptDurationMs(Long transcriptDurationMs) {
        this.transcriptDurationMs =
                safeSet("transcriptDurationMs", this.transcriptDurationMs, transcriptDurationMs);
    }

    public void setLlmGenerationMs(Long llmGenerationMs) {
        this.llmGenerationMs = safeSet("llmGenerationMs", this.llmGenerationMs, llmGenerationMs);
    }

    public void setTtsSynthesisMs(Long ttsSynthesisMs) {
        this.ttsSynthesisMs = safeSet("ttsSynthesisMs", this.ttsSynthesisMs, ttsSynthesisMs);
    }

    public void setTtsSpeechDurationMs(Long ttsSpeechDurationMs) {
        this.ttsSpeechDurationMs =
                safeSet("ttsSpeechDurationMs", this.ttsSpeechDurationMs, ttsSpeechDurationMs);
    }

    public void setUserSpeechDurationMs(Long userSpeechDurationMs) {
        this.userSpeechDurationMs =
                safeSet("userSpeechDurationMs", this.userSpeechDurationMs, userSpeechDurationMs);
    }

    protected void log(TurnOutcome outcome) {
        String message = String.format("TURN metrics | correlationId=%s", correlationId);
        long totalTurnMs = 0L;
        if (userSpeechStartAt != null) {
            totalTurnMs += Duration.between(userSpeechStartAt, Instant.now()).toMillis();
        } else if (userUtteranceAcceptedAt != null) {
            totalTurnMs += Duration.between(userUtteranceAcceptedAt, Instant.now()).toMillis();
        }

        if (outcome == null) {
            outcome = TurnOutcome.UNKNOWN;
        }
        message += String.format(" outcome=%s", outcome);

        message += String.format(" totalTurnMs=%d", totalTurnMs);
        message += String.format(" startedFrom=%s", metricsStartSource());
        if (userSpeechDurationMs != null) {
            message += String.format(" userSpeechDurationMs=%d", userSpeechDurationMs);
        }
        if (transcriptDurationMs != null) {
            message += String.format(" transcriptionDurationMs=%d", transcriptDurationMs);
        }
        if (speechToTranscriptMs != null) {
            message += String.format(" speechToTranscriptMs=%d", speechToTranscriptMs);
        }
        if (llmGenerationMs != null) {
            message += String.format(" llmGenerationMs=%d", llmGenerationMs);
        }
        if (ttsSynthesisMs != null) {
            message += String.format(" ttsSynthesisMs=%d", ttsSynthesisMs);
        }
        if (userUtteranceAcceptedAt != null && ttsSpeechStartAt != null) {
            message +=
                    String.format(
                            " assistantFirstAudioLatencyMs=%d",
                            Duration.between(userUtteranceAcceptedAt, ttsSpeechStartAt).toMillis());
        }
        if (ttsSpeechDurationMs != null) {
            message += String.format(" ttsSpeechDurationMs=%d", ttsSpeechDurationMs);
        }
        perfLog.info(message);
    }

    private String metricsStartSource() {
        if (userSpeechStartAt != null) {
            return "USER_SPEECH_STARTED";
        }

        if (userUtteranceAcceptedAt != null) {
            return "USER_UTTERANCE_ACCEPTED";
        }

        return "UNKNOWN";
    }

    private <T> T safeSet(String attributeName, T currentValue, T newValue) {
        if (currentValue != null) {
            errorLog.error(
                    "Ignoring metric overwrite | correlationId={} attribute={} current={} new={}",
                    correlationId,
                    attributeName,
                    currentValue,
                    newValue);

            return currentValue;
        }

        return newValue;
    }
}
