from __future__ import annotations

import time
from dataclasses import dataclass
from typing import Optional

from app.audio.microphone_capture import MicrophoneCaptureService
from app.audio.speech_segment import SpeechSegment
from app.gatekeeper.gatekeeper_decision import GatekeeperDecision
from app.gatekeeper.transcript_gatekeeper import TranscriptGatekeeper
from app.stt.stt_result import SttResult
from app.stt.whisper_service import WhisperService
from app.transport.orchestrator_client import CallbackResult, OrchestratorClient
from app.transport.stt_events import PreparedSttEvent, build_stt_event


@dataclass(frozen=True)
class PipelineResult:
    success: bool
    segment: SpeechSegment
    stt: SttResult
    decision: GatekeeperDecision
    event: PreparedSttEvent
    callback: Optional[CallbackResult]
    timings: dict

    def to_dict(self) -> dict:
        return {
            "success": self.success,
            "audioFile": self.segment.audio_file,
            "segment": self.segment.to_dict(),
            "stt": self.stt.to_dict(),
            "decision": self.decision.to_dict(),
            "event": self.event.to_dict(),
            "callback": self.callback.to_dict() if self.callback else None,
            "timings": self.timings,
        }


class SttPipeline:
    def __init__(
        self,
        capture_service: MicrophoneCaptureService,
        whisper_service: WhisperService,
        gatekeeper: TranscriptGatekeeper,
        orchestrator_client: OrchestratorClient,
    ):
        self.capture_service = capture_service
        self.whisper_service = whisper_service
        self.gatekeeper = gatekeeper
        self.orchestrator_client = orchestrator_client

    def capture_transcribe_and_decide(self) -> PipelineResult:
        total_started = time.monotonic()

        capture_started = time.monotonic()
        segment = self.capture_service.capture_once()
        capture_ms = int((time.monotonic() - capture_started) * 1000)

        pre_decision = self.gatekeeper.pre_evaluate_segment(segment)

        if pre_decision is not None:
            stt_result = SttResult(
                success=False,
                text=None,
                language=None,
                language_probability=None,
                error_code="STT_SKIPPED",
                error_message=f"Whisper skipped because segment was rejected before transcription: {pre_decision.reason}",
            )
            decision = pre_decision
            whisper_ms = 0
        else:
            whisper_started = time.monotonic()
            stt_result = self.whisper_service.transcribe(segment.audio_file)
            whisper_ms = int((time.monotonic() - whisper_started) * 1000)

            decision = self.gatekeeper.evaluate(segment, stt_result)

        event = build_stt_event(segment, stt_result, decision)

        total_ms = int((time.monotonic() - total_started) * 1000)

        return PipelineResult(
            success=True,
            segment=segment,
            stt=stt_result,
            decision=decision,
            event=event,
            callback=None,
            timings={
                "captureMs": capture_ms,
                "whisperMs": whisper_ms,
                "totalMs": total_ms,
                "whisperSkipped": pre_decision is not None,
            },
        )

    def capture_transcribe_decide_and_callback(self) -> PipelineResult:
        result = self.capture_transcribe_and_decide()
        callback_result = self.orchestrator_client.send_stt_event(result.event)

        return PipelineResult(
            result.success and callback_result.success,
            result.segment,
            result.stt,
            result.decision,
            result.event,
            callback_result,
            result.timings,
        )
