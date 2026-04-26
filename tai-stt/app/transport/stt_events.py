from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime, timezone
from uuid import uuid4

from app.audio.speech_segment import SpeechSegment
from app.gatekeeper.gatekeeper_decision import GatekeeperDecision, RejectionCategory
from app.stt.stt_result import SttResult


@dataclass(frozen=True)
class PreparedSttEvent:
    event_type: str
    callback_name: str
    payload: dict

    def to_dict(self) -> dict:
        return {
            "eventType": self.event_type,
            "callbackName": self.callback_name,
            "payload": self.payload,
        }


def build_stt_event(
    segment: SpeechSegment,
    stt_result: SttResult,
    decision: GatekeeperDecision,
    correlation_id: str | None = None,
) -> PreparedSttEvent:
    correlation_id = correlation_id or str(uuid4())

    base_payload = {
        "eventId": str(uuid4()),
        "createdAt": datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
        "source": "STT_SERVICE",
        "correlationId": correlation_id,
        "language": stt_result.language,
        "languageProbability": stt_result.language_probability,
        "durationMs": segment.duration_ms,
        "averageEnergy": segment.average_energy,
        "reason": decision.reason,
        "suspicionScore": decision.suspicion_score,
    }

    if decision.accepted:
        payload = dict(base_payload)
        payload["text"] = stt_result.text
        return PreparedSttEvent("STT_TRANSCRIPT_ACCEPTED", "transcript_accepted", payload)

    if decision.rejection_category == RejectionCategory.UNINTELLIGIBLE:
        return PreparedSttEvent("STT_TRANSCRIPT_UNINTELLIGIBLE", "transcript_unintelligible", base_payload)

    return PreparedSttEvent("STT_TRANSCRIPT_NOISE", "transcript_noise", base_payload)
