package com.toteuch.tai.stt.listener.gatekeeper;

import static org.assertj.core.api.Assertions.assertThat;

import com.toteuch.tai.stt.listener.audio.SpeechSegment;
import com.toteuch.tai.stt.listener.config.SttListenerProperties;
import com.toteuch.tai.stt.listener.transcription.TranscriptionResult;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TranscriptGatekeeperTest {
    private TranscriptGatekeeper gatekeeper;

    @BeforeEach
    void setUp() {
        SttListenerProperties properties = new SttListenerProperties();
        gatekeeper = new TranscriptGatekeeper(properties);
    }

    @Test
    void should_reject_when_no_speech_detected() {
        GatekeeperDecision decision =
                gatekeeper.preEvaluateSegment(segment(false, false, 3000, 1, 1, 0));

        assertThat(decision.accepted()).isFalse();
        assertThat(decision.reason()).isEqualTo("NO_SPEECH_DETECTED");
        assertThat(decision.rejectionCategory()).isEqualTo(RejectionCategory.NOISE);
    }

    @Test
    void should_reject_when_audio_is_too_weak() {
        GatekeeperDecision decision =
                gatekeeper.preEvaluateSegment(segment(true, true, 3000, 10, 10, 0.5));

        assertThat(decision.accepted()).isFalse();
        assertThat(decision.reason()).isEqualTo("AUDIO_TOO_WEAK");
        assertThat(decision.rejectionCategory()).isEqualTo(RejectionCategory.NOISE);
    }

    @Test
    void should_reject_when_not_enough_voiced_audio() {
        GatekeeperDecision decision =
                gatekeeper.preEvaluateSegment(segment(true, true, 3000, 80, 300, 0.05));

        assertThat(decision.accepted()).isFalse();
        assertThat(decision.reason()).isEqualTo("NOT_ENOUGH_VOICED_AUDIO");
        assertThat(decision.rejectionCategory()).isEqualTo(RejectionCategory.NOISE);
    }

    @Test
    void should_not_reject_short_audible_utterance_before_transcription() {
        GatekeeperDecision decision =
                gatekeeper.preEvaluateSegment(segment(true, true, 2048, 21.57, 170.60, 0.125));

        assertThat(decision).isNull();
    }

    @Test
    void should_accept_valid_french_transcription() {
        GatekeeperDecision decision =
                gatekeeper.evaluate(
                        segment(true, true, 2500, 120, 400, 0.5),
                        TranscriptionResult.success("Bonjour Tai", "fr", 0.95));

        assertThat(decision.accepted()).isTrue();
        assertThat(decision.reason()).isEqualTo("ACCEPTED");
        assertThat(decision.rejectionCategory()).isEqualTo(RejectionCategory.NONE);
    }

    @Test
    void should_reject_unsupported_language_as_unintelligible() {
        GatekeeperDecision decision =
                gatekeeper.evaluate(
                        segment(true, true, 2500, 120, 400, 0.5),
                        TranscriptionResult.success("こんにちは", "ja", 0.90));

        assertThat(decision.accepted()).isFalse();
        assertThat(decision.reason()).isEqualTo("UNSUPPORTED_LANGUAGE");
        assertThat(decision.rejectionCategory()).isEqualTo(RejectionCategory.UNINTELLIGIBLE);
    }

    @Test
    void should_map_empty_transcript_with_speech_to_unintelligible() {
        GatekeeperDecision decision =
                gatekeeper.evaluate(
                        segment(true, true, 2500, 120, 400, 0.5),
                        TranscriptionResult.success("", "fr", 0.80));

        assertThat(decision.accepted()).isFalse();
        assertThat(decision.reason()).isEqualTo("EMPTY_TRANSCRIPT");
        assertThat(decision.rejectionCategory()).isEqualTo(RejectionCategory.UNINTELLIGIBLE);
    }

    private SpeechSegment segment(
            boolean speechStarted,
            boolean speechEnded,
            long durationMs,
            double averageEnergy,
            double peakEnergy,
            double voicedRatio) {
        return new SpeechSegment(
                Path.of("input/test.wav"),
                durationMs,
                averageEnergy,
                peakEnergy,
                voicedRatio,
                speechStarted,
                speechEnded);
    }
}
