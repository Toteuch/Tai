# SPDX-License-Identifier: GPL-3.0-only
from typing import Optional

from pydantic import BaseModel, Field


class TranscribeFileRequest(BaseModel):
    correlation_id: Optional[str] = Field(default=None, alias="correlationId")
    audio_file: str = Field(alias="audioFile")


class TranscriptionResponse(BaseModel):
    success: bool
    correlation_id: Optional[str] = Field(default=None, alias="correlationId")
    text: Optional[str] = None
    language: Optional[str] = None
    language_probability: Optional[float] = Field(default=None, alias="languageProbability")
    transcription_duration_ms: Optional[int] = Field(default=None, alias="transcriptionDurationMs")
    model_name: Optional[str] = Field(default=None, alias="modelName")
    error_code: Optional[str] = Field(default=None, alias="errorCode")
    error_message: Optional[str] = Field(default=None, alias="errorMessage")

    model_config = {
        "populate_by_name": True,
    }


class HealthResponse(BaseModel):
    status: str
    model_loaded: bool = Field(alias="modelLoaded")
    model_size: str = Field(alias="modelSize")
    device: str
    compute_type: str = Field(alias="computeType")
    last_error: Optional[str] = Field(default=None, alias="lastError")

    model_config = {
        "populate_by_name": True,
    }


class TranscribeBytesRequest(BaseModel):
    correlation_id: Optional[str] = Field(default=None, alias="correlationId")
    filename: Optional[str] = None
    audio_base64: str = Field(alias="audioBase64")

    model_config = {
        "populate_by_name": True,
    }
