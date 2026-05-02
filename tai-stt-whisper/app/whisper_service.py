import time
from pathlib import Path
from typing import Optional

from faster_whisper import WhisperModel

from app.config import AppSettings
from app.models import TranscriptionResponse


class WhisperTranscriber:
    def __init__(self, settings: AppSettings):
        self._settings = settings
        self._model: Optional[WhisperModel] = None
        self._last_error: Optional[str] = None

    @property
    def model_loaded(self) -> bool:
        return self._model is not None

    @property
    def last_error(self) -> Optional[str]:
        return self._last_error

    def load_model(self) -> None:
        try:
            whisper_settings = self._settings.whisper
            self._model = WhisperModel(
                whisper_settings.model_size,
                device=whisper_settings.device,
                compute_type=whisper_settings.compute_type,
            )
            self._last_error = None
        except Exception as exc:
            self._model = None
            self._last_error = str(exc)
            raise

    def transcribe(self, audio_file: Path, correlation_id: Optional[str] = None) -> TranscriptionResponse:
        if self._model is None:
            return TranscriptionResponse(
                success=False,
                correlationId=correlation_id,
                errorCode="MODEL_NOT_LOADED",
                errorMessage="Whisper model is not loaded.",
                modelName=self._settings.whisper.model_size,
            )

        if not audio_file.exists():
            return TranscriptionResponse(
                success=False,
                correlationId=correlation_id,
                errorCode="AUDIO_FILE_NOT_FOUND",
                errorMessage=f"Audio file does not exist: {audio_file}",
                modelName=self._settings.whisper.model_size,
            )

        started = time.monotonic()

        try:
            language = self._settings.whisper.language
            if language is not None and language.strip() == "":
                language = None

            segments, info = self._model.transcribe(
                str(audio_file),
                beam_size=self._settings.whisper.beam_size,
                vad_filter=self._settings.whisper.vad_filter,
                temperature=self._settings.whisper.temperature,
                condition_on_previous_text=self._settings.whisper.condition_on_previous_text,
                initial_prompt=self._settings.whisper.initial_prompt,
                language=language,
            )

            text = "".join(segment.text for segment in segments).strip()
            duration_ms = int((time.monotonic() - started) * 1000)

            return TranscriptionResponse(
                success=True,
                correlationId=correlation_id,
                text=text,
                language=getattr(info, "language", None),
                languageProbability=getattr(info, "language_probability", None),
                transcriptionDurationMs=duration_ms,
                modelName=self._settings.whisper.model_size,
            )
        except Exception as exc:
            duration_ms = int((time.monotonic() - started) * 1000)
            self._last_error = str(exc)

            return TranscriptionResponse(
                success=False,
                correlationId=correlation_id,
                transcriptionDurationMs=duration_ms,
                modelName=self._settings.whisper.model_size,
                errorCode="WHISPER_TRANSCRIPTION_ERROR",
                errorMessage=str(exc),
            )
