package com.toteuch.tai.taiorchestrator.services.stt.audio.gatekeeper;

import com.toteuch.tai.taiorchestrator.services.stt.SttResult;
import com.toteuch.tai.taiorchestrator.services.stt.audio.vad.SpeechSegment;
import org.springframework.stereotype.Component;

@Component
public class DefaultTranscriptGatekeeper implements TranscriptGatekeeper {

    private final TranscriptGatekeeperProperties properties;

    public DefaultTranscriptGatekeeper(TranscriptGatekeeperProperties properties) {
        this.properties = properties;
    }

    @Override
    public GatekeeperDecision evaluate(SpeechSegment segment, SttResult sttResult) {
        if (segment == null) {
            return new GatekeeperDecision(false, "SEGMENT_MISSING", 999, RejectionCategory.NOISE);
        }

        if (sttResult == null || !sttResult.success()) {
            return new GatekeeperDecision(false, "STT_FAILED", 999, RejectionCategory.NOISE);
        }

        String text = normalize(sttResult.text());

        if (text == null || text.isBlank()) {
            return new GatekeeperDecision(false, "EMPTY_TRANSCRIPT", 999, RejectionCategory.NOISE);
        }

        if (!containsAlphaNumeric(text)) {
            return new GatekeeperDecision(false, "NO_ALPHANUMERIC_CONTENT", 999, RejectionCategory.NOISE);
        }

        if (segment.durationMs() < properties.getRejectAudioDurationMs()) {
            return new GatekeeperDecision(false, "AUDIO_TOO_SHORT", 999, RejectionCategory.NOISE);
        }

        if (segment.averageEnergy() < properties.getRejectAverageEnergyThreshold()) {
            return new GatekeeperDecision(false, "AUDIO_TOO_WEAK", 999, RejectionCategory.NOISE);
        }

        if (sttResult.language() != null
            && !properties.getAllowedLanguages().contains(sttResult.language())) {
            return new GatekeeperDecision(false, "UNSUPPORTED_LANGUAGE", 999, RejectionCategory.UNINTELLIGIBLE);
        }

        int suspicionScore = 0;

        if (segment.durationMs() < properties.getSuspiciousAudioDurationMs()) {
            suspicionScore++;
        }

        if (sttResult.languageProbability() != null
            && sttResult.languageProbability() < properties.getSuspiciousLanguageProbabilityThreshold()) {
            suspicionScore++;
        }

        if (isVeryShortSingleToken(text)) {
            suspicionScore++;
        }

        if (hasLowAlphaNumericRatio(text)) {
            suspicionScore++;
        }

        if (text.isBlank()) {
            return new GatekeeperDecision(false, "EMPTY_TRANSCRIPT", 999, RejectionCategory.NOISE);
        }

        if (segment.durationMs() < properties.getRejectAudioDurationMs()) {
            return new GatekeeperDecision(false, "AUDIO_TOO_SHORT", 999, RejectionCategory.NOISE);
        }

        if (segment.averageEnergy() < properties.getRejectAverageEnergyThreshold()) {
            return new GatekeeperDecision(false, "AUDIO_TOO_WEAK", 999, RejectionCategory.NOISE);
        }

        if (suspicionScore >= properties.getRejectSuspicionScore()) {
            return new GatekeeperDecision(false, "SUSPICIOUS_SEGMENT", suspicionScore, RejectionCategory.UNINTELLIGIBLE);
        }

        return new GatekeeperDecision(true, "ACCEPTED", suspicionScore, RejectionCategory.NONE);
    }

    private String normalize(String text) {
        return text == null ? null : text.trim();
    }

    private boolean containsAlphaNumeric(String text) {
        return text.codePoints().anyMatch(Character::isLetterOrDigit);
    }

    private boolean isVeryShortSingleToken(String text) {
        String[] tokens = text.trim().split("\\s+");
        return tokens.length == 1 && text.length() <= 2;
    }

    private boolean hasLowAlphaNumericRatio(String text) {
        long total = text.length();
        if (total == 0) {
            return true;
        }

        long alphaNum = text.codePoints().filter(Character::isLetterOrDigit).count();
        double ratio = (double) alphaNum / (double) total;
        return ratio < 0.4;
    }
}
