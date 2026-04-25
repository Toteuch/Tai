from dataclasses import dataclass

from app.audio.microphone_capture import MicrophoneCaptureService
from app.config import Settings
from app.gatekeeper.transcript_gatekeeper import TranscriptGatekeeper
from app.pipeline.stt_pipeline import SttPipeline
from app.stt.whisper_service import WhisperService
from app.transport.orchestrator_client import OrchestratorClient


@dataclass
class Runtime:
    settings: Settings
    capture_service: MicrophoneCaptureService
    whisper_service: WhisperService
    gatekeeper: TranscriptGatekeeper
    orchestrator_client: OrchestratorClient
    pipeline: SttPipeline

    @classmethod
    def create(cls, settings: Settings) -> "Runtime":
        capture_service = MicrophoneCaptureService(settings.capture)
        whisper_service = WhisperService(settings.whisper)
        gatekeeper = TranscriptGatekeeper(settings.gatekeeper)
        orchestrator_client = OrchestratorClient(settings.orchestrator)

        pipeline = SttPipeline(
            capture_service=capture_service,
            whisper_service=whisper_service,
            gatekeeper=gatekeeper,
            orchestrator_client=orchestrator_client,
        )

        return cls(
            settings=settings,
            capture_service=capture_service,
            whisper_service=whisper_service,
            gatekeeper=gatekeeper,
            orchestrator_client=orchestrator_client,
            pipeline=pipeline,
        )

    def shutdown(self) -> None:
        self.capture_service.shutdown()
