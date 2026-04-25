from __future__ import annotations

from pathlib import Path
from threading import Lock

from app.config import WhisperSettings
from app.stt.stt_result import SttResult


class WhisperService:
    def __init__(self, settings: WhisperSettings):
        self.settings = settings
        self._model = None
        self._lock = Lock()

    def load_model(self) -> None:
        if not self.settings.enabled:
            return

        with self._lock:
            if self._model is not None:
                return

            from faster_whisper import WhisperModel

            self._model = WhisperModel(
                self.settings.model_size,
                device=self.settings.device,
                compute_type=self.settings.compute_type,
            )

    def transcribe(self, audio_file: str) -> SttResult:
        if not self.settings.enabled:
            return SttResult(False, None, None, None, "WHISPER_DISABLED", "Whisper STT is disabled")

        self.load_model()

        if self._model is None:
            return SttResult(False, None, None, None, "WHISPER_MODEL_NOT_LOADED", "Whisper model is not loaded")

        try:
            segments, info = self._model.transcribe(str(Path(audio_file)))
            text = " ".join(segment.text.strip() for segment in segments).strip()

            return SttResult(
                success=True,
                text=text,
                language=getattr(info, "language", None),
                language_probability=getattr(info, "language_probability", None),
            )
        except Exception as exc:
            return SttResult(False, None, None, None, "WHISPER_TRANSCRIPTION_FAILED", str(exc))
