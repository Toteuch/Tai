package com.toteuch.tai.stt.listener.gatekeeper;

import com.toteuch.tai.stt.listener.audio.SpeechSegment;
import com.toteuch.tai.stt.listener.config.SttListenerProperties;
import com.toteuch.tai.stt.listener.transcription.TranscriptionResult;
import org.springframework.stereotype.Service;

@Service
public class TranscriptGatekeeper {
    private final SttListenerProperties properties;

    public TranscriptGatekeeper(SttListenerProperties properties) {
        this.properties = properties;
    }

    public GatekeeperDecision preEvaluateSegment(SpeechSegment segment) {
        SttListenerProperties.Gatekeeper gatekeeper = properties.getGatekeeper();

        if (segment == null) {
            return GatekeeperDecision.noise("SEGMENT_MISSING");
        }

        if (!segment.speechStarted()) {
            return GatekeeperDecision.noise("NO_SPEECH_DETECTED");
        }

        if (segment.durationMs() < gatekeeper.getRejectAudioDurationMs()) {
            return GatekeeperDecision.noise("AUDIO_TOO_SHORT");
        }

        if (segment.averageEnergy() < gatekeeper.getRejectAverageEnergyThreshold()) {
            return GatekeeperDecision.noise("AUDIO_TOO_WEAK");
        }

        if (segment.voicedRatio() < gatekeeper.getMinVoicedRatio()) {
            return GatekeeperDecision.noise("NOT_ENOUGH_VOICED_AUDIO");
        }

        return null;
    }

    public GatekeeperDecision evaluate(SpeechSegment segment, TranscriptionResult transcription) {
        GatekeeperDecision preDecision = preEvaluateSegment(segment);
        if (preDecision != null) {
            return preDecision;
        }

        if (transcription == null || !transcription.success()) {
            return GatekeeperDecision.noise("STT_FAILED");
        }

        String text = normalize(transcription.text());

        if (text == null || text.isBlank()) {
            if (segment.speechStarted()
                    && segment.averageEnergy()
                            >= properties.getGatekeeper().getRejectAverageEnergyThreshold()) {
                return GatekeeperDecision.unintelligible("EMPTY_TRANSCRIPT");
            }
            return GatekeeperDecision.noise("EMPTY_TRANSCRIPT");
        }

        if (!containsAlphanumeric(text)) {
            return GatekeeperDecision.noise("NO_ALPHANUMERIC_CONTENT");
        }

        String language = transcription.language();
        if (language != null
                && !properties.getGatekeeper().getAllowedLanguages().contains(language)) {
            return GatekeeperDecision.unintelligible("UNSUPPORTED_LANGUAGE");
        }

        int suspicionScore = 0;

        if (segment.durationMs() < properties.getGatekeeper().getSuspiciousAudioDurationMs()) {
            suspicionScore++;
        }

        Double languageProbability = transcription.languageProbability();
        if (languageProbability != null
                && languageProbability
                        < properties.getGatekeeper().getSuspiciousLanguageProbabilityThreshold()) {
            suspicionScore++;
        }

        if (isVeryShortSingleToken(text)) {
            suspicionScore++;
        }

        if (hasLowAlphanumericRatio(text)) {
            suspicionScore++;
        }

        if (suspicionScore >= properties.getGatekeeper().getRejectSuspicionScore()) {
            return GatekeeperDecision.suspicious("SUSPICIOUS_SEGMENT", suspicionScore);
        }

        return GatekeeperDecision.accepted(suspicionScore);
    }

    private String normalize(String text) {
        return text == null ? null : text.trim();
    }

    private boolean containsAlphanumeric(String text) {
        return text.chars().anyMatch(Character::isLetterOrDigit);
    }

    private boolean isVeryShortSingleToken(String text) {
        String[] tokens = text.trim().split("\\s+");
        return tokens.length == 1 && text.length() <= 2;
    }

    private boolean hasLowAlphanumericRatio(String text) {
        if (text.isEmpty()) {
            return true;
        }
        long alphanumericCount = text.chars().filter(Character::isLetterOrDigit).count();
        double ratio = (double) alphanumericCount / text.length();
        return ratio < 0.4;
    }
}
