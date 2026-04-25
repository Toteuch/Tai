from __future__ import annotations

from app.audio.speech_segment import SpeechSegment
from app.config import GatekeeperSettings
from app.gatekeeper.gatekeeper_decision import GatekeeperDecision, RejectionCategory
from app.stt.stt_result import SttResult


class TranscriptGatekeeper:
    def __init__(self, settings: GatekeeperSettings):
        self.settings = settings

    def evaluate(self, segment: SpeechSegment | None, stt_result: SttResult | None) -> GatekeeperDecision:
        if segment is None:
            return GatekeeperDecision(False, "SEGMENT_MISSING", 999, RejectionCategory.NOISE)

        if stt_result is None or not stt_result.success:
            return GatekeeperDecision(False, "STT_FAILED", 999, RejectionCategory.NOISE)

        text = self._normalize(stt_result.text)

        if text is None or not text.strip():
            if segment.speech_started and segment.average_energy >= self.settings.reject_average_energy_threshold:
                return GatekeeperDecision(False, "EMPTY_TRANSCRIPT", 999, RejectionCategory.UNINTELLIGIBLE)

            return GatekeeperDecision(False, "EMPTY_TRANSCRIPT", 999, RejectionCategory.NOISE)

        if not self._contains_alphanumeric(text):
            return GatekeeperDecision(False, "NO_ALPHANUMERIC_CONTENT", 999, RejectionCategory.NOISE)

        if segment.duration_ms < self.settings.reject_audio_duration_ms:
            return GatekeeperDecision(False, "AUDIO_TOO_SHORT", 999, RejectionCategory.NOISE)

        if segment.average_energy < self.settings.reject_average_energy_threshold:
            return GatekeeperDecision(False, "AUDIO_TOO_WEAK", 999, RejectionCategory.NOISE)

        if stt_result.language is not None and stt_result.language not in self.settings.allowed_languages:
            return GatekeeperDecision(False, "UNSUPPORTED_LANGUAGE", 999, RejectionCategory.UNINTELLIGIBLE)

        suspicion_score = 0

        if segment.duration_ms < self.settings.suspicious_audio_duration_ms:
            suspicion_score += 1

        if (
            stt_result.language_probability is not None
            and stt_result.language_probability < self.settings.suspicious_language_probability_threshold
        ):
            suspicion_score += 1

        if self._is_very_short_single_token(text):
            suspicion_score += 1

        if self._has_low_alphanumeric_ratio(text):
            suspicion_score += 1

        if suspicion_score >= self.settings.reject_suspicion_score:
            return GatekeeperDecision(False, "SUSPICIOUS_SEGMENT", suspicion_score, RejectionCategory.UNINTELLIGIBLE)

        return GatekeeperDecision(True, "ACCEPTED", suspicion_score, RejectionCategory.NONE)

    def pre_evaluate_segment(self, segment: SpeechSegment | None) -> GatekeeperDecision | None:
        if segment is None:
            return GatekeeperDecision(False, "SEGMENT_MISSING", 999, RejectionCategory.NOISE)

        if not segment.speech_started:
            return GatekeeperDecision(False, "NO_SPEECH_DETECTED", 999, RejectionCategory.NOISE)

        if segment.duration_ms < self.settings.reject_audio_duration_ms:
            return GatekeeperDecision(False, "AUDIO_TOO_SHORT", 999, RejectionCategory.NOISE)

        if segment.average_energy < self.settings.reject_average_energy_threshold:
            return GatekeeperDecision(False, "AUDIO_TOO_WEAK", 999, RejectionCategory.NOISE)

        if segment.voiced_ratio < self.settings.min_voiced_ratio:
            return GatekeeperDecision(False, "NOT_ENOUGH_VOICED_AUDIO", 999, RejectionCategory.NOISE)

        return None

    def _normalize(self, text: str | None) -> str | None:
        return None if text is None else text.strip()

    def _contains_alphanumeric(self, text: str) -> bool:
        return any(ch.isalnum() for ch in text)

    def _is_very_short_single_token(self, text: str) -> bool:
        tokens = text.strip().split()
        return len(tokens) == 1 and len(text) <= 2

    def _has_low_alphanumeric_ratio(self, text: str) -> bool:
        total = len(text)
        if total == 0:
            return True

        alpha_num = sum(1 for ch in text if ch.isalnum())
        ratio = alpha_num / total
        return ratio < 0.4
