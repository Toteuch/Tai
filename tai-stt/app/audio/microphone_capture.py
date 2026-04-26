from __future__ import annotations

import time
import wave
from pathlib import Path
from typing import List

import numpy as np
import sounddevice as sd

from app.audio.speech_segment import SpeechSegment
from app.config import CaptureSettings


class MicrophoneCaptureService:
    def __init__(self, settings: CaptureSettings):
        self.settings = settings
        self.output_dir = Path(settings.output_dir)
        self.output_dir.mkdir(parents=True, exist_ok=True)

    def capture_once(self) -> SpeechSegment:
        output_file = self.output_dir / f"mic_{int(time.time() * 1000)}.wav"

        frames: List[np.ndarray] = []
        energies: List[float] = []

        speech_started = False
        speech_ended = False
        started_at = time.monotonic()
        last_voice_at = started_at

        block_size = self.settings.buffer_size

        with sd.InputStream(
            samplerate=self.settings.sample_rate,
            channels=self.settings.channels,
            dtype="int16",
            blocksize=block_size,
        ) as stream:
            while True:
                data, _overflowed = stream.read(block_size)
                chunk = np.asarray(data, dtype=np.int16)

                energy = float(np.mean(np.abs(chunk)))
                energies.append(energy)
                frames.append(chunk.copy())

                now = time.monotonic()
                elapsed_ms = int((now - started_at) * 1000)

                if not speech_started and elapsed_ms >= self.settings.no_speech_timeout_ms:
                    break

                if energy >= self.settings.silence_threshold:
                    speech_started = True
                    last_voice_at = now

                silence_ms = int((now - last_voice_at) * 1000)

                if (
                    speech_started
                    and elapsed_ms >= self.settings.min_recording_ms
                    and silence_ms >= self.settings.silence_duration_ms
                ):
                    speech_ended = True
                    break

                if elapsed_ms >= self.settings.max_recording_ms:
                    break

        peak_energy = float(max(energies)) if energies else 0.0
        voiced_chunks = sum(1 for energy in energies if energy >= self.settings.silence_threshold)
        voiced_ratio = float(voiced_chunks / len(energies)) if energies else 0.0
        audio = np.concatenate(frames, axis=0) if frames else np.zeros((0, self.settings.channels), dtype=np.int16)
        duration_ms = int((len(audio) / self.settings.sample_rate) * 1000)
        if len(audio) > 0:
            audio_float = audio.astype(np.float32)
            average_energy = float(np.sqrt(np.mean(np.square(audio_float))))
        else:
            average_energy = 0.0

        self._write_wav(output_file, audio)

        return SpeechSegment(
            audio_file=str(output_file),
            duration_ms=duration_ms,
            average_energy=average_energy,
            peak_energy=peak_energy,
            voiced_ratio=voiced_ratio,
            speech_started=speech_started,
            speech_ended=speech_ended,
        )

    def _write_wav(self, output_file: Path, audio: np.ndarray) -> None:
        with wave.open(str(output_file), "wb") as wav:
            wav.setnchannels(self.settings.channels)
            wav.setsampwidth(self.settings.sample_size_bits // 8)
            wav.setframerate(self.settings.sample_rate)
            wav.writeframes(audio.astype(np.int16).tobytes())

    def shutdown(self) -> None:
        pass
