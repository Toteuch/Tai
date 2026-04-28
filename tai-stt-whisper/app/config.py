# SPDX-License-Identifier: GPL-3.0-only
from pathlib import Path
from typing import Any, Optional

import yaml
from pydantic import BaseModel


BASE_DIR = Path(__file__).resolve().parent.parent
DEFAULT_CONFIG_PATH = BASE_DIR / "config.yaml"


class ServerSettings(BaseModel):
    host: str = "127.0.0.1"
    port: int = 8095


class WhisperSettings(BaseModel):
    model_size: str = "small"
    device: str = "cpu"
    compute_type: str = "int8"
    beam_size: int = 5
    temperature: float = 0.0
    condition_on_previous_text: bool = False
    vad_filter: bool = False
    language: str | None = "en"
    initial_prompt: str | None = "The assistant is named Tai. English is expected. Common words: Tai, Thaï, LLM, TTS, STT."


class StorageSettings(BaseModel):
    temp_dir: str = "./tmp"


class AppSettings(BaseModel):
    server: ServerSettings = ServerSettings()
    whisper: WhisperSettings = WhisperSettings()
    storage: StorageSettings = StorageSettings()


def _normalize_keys(value: Any) -> Any:
    if isinstance(value, dict):
        return {
            str(key).replace("-", "_"): _normalize_keys(item)
            for key, item in value.items()
        }

    if isinstance(value, list):
        return [_normalize_keys(item) for item in value]

    return value


def load_settings(config_path: Path = DEFAULT_CONFIG_PATH) -> AppSettings:
    if not config_path.exists():
        raise FileNotFoundError(f"Missing STT Whisper configuration file: {config_path}")

    with config_path.open("r", encoding="utf-8") as file:
        raw_config = yaml.safe_load(file) or {}

    return AppSettings.model_validate(_normalize_keys(raw_config))
