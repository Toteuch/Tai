from pathlib import Path
from typing import Dict, List

import yaml
from pydantic import BaseModel, Field


class ServerSettings(BaseModel):
    host: str = "127.0.0.1"
    port: int = 8091


class OrchestratorSettings(BaseModel):
    base_url: str = "http://localhost:8080"
    callbacks: Dict[str, str]


class WhisperSettings(BaseModel):
    enabled: bool = True
    model_size: str = "small"
    device: str = "cpu"
    compute_type: str = "int8"


class CaptureSettings(BaseModel):
    output_dir: str = "./tai-stt/input"
    sample_rate: int = 16000
    sample_size_bits: int = 16
    channels: int = 1
    signed: bool = True
    big_endian: bool = False
    buffer_size: int = 4096
    silence_threshold: int = 500
    silence_duration_ms: int = 1200
    min_recording_ms: int = 800
    max_recording_ms: int = 15000
    no_speech_timeout_ms: int = 3000
    min_peak_to_average_ratio_for_noise:float = 4.0


class GatekeeperSettings(BaseModel):
    allowed_languages: List[str] = Field(default_factory=lambda: ["en", "fr"])
    reject_audio_duration_ms: int = 250
    suspicious_audio_duration_ms: int = 500
    reject_average_energy_threshold: int = 150
    suspicious_language_probability_threshold: float = 0.45
    reject_suspicion_score: int = 2
    min_voiced_ratio: float = 0.20


class Settings(BaseModel):
    server: ServerSettings
    orchestrator: OrchestratorSettings
    whisper: WhisperSettings
    capture: CaptureSettings
    gatekeeper: GatekeeperSettings


def load_settings(path: str = "config.yaml") -> Settings:
    config_path = Path(path)
    if not config_path.exists():
        raise FileNotFoundError(f"Missing STT configuration file: {config_path.resolve()}")

    with config_path.open("r", encoding="utf-8") as f:
        data = yaml.safe_load(f)

    return Settings.model_validate(data)
