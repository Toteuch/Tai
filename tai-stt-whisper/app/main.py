# SPDX-License-Identifier: GPL-3.0-only
from contextlib import asynccontextmanager
from pathlib import Path
from fastapi import Header, Request
from app.models import HealthResponse, TranscribeFileRequest, TranscribeBytesRequest, TranscriptionResponse
import shutil
import uuid
import base64

from fastapi import FastAPI, File, Form, HTTPException, UploadFile

from app.config import load_settings
from app.models import HealthResponse, TranscribeFileRequest, TranscriptionResponse
from app.whisper_service import WhisperTranscriber


settings = load_settings()
transcriber = WhisperTranscriber(settings)


@asynccontextmanager
async def lifespan(app: FastAPI):
    transcriber.load_model()
    Path(settings.storage.temp_dir).mkdir(parents=True, exist_ok=True)
    yield


app = FastAPI(
    title="Tai STT Whisper Service",
    description="Pure Whisper transcription microservice. No capture, no gatekeeper, no orchestrator callback.",
    version="0.1.0",
    lifespan=lifespan,
)


@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    status = "UP" if transcriber.model_loaded else "DOWN"

    return HealthResponse(
        status=status,
        modelLoaded=transcriber.model_loaded,
        modelSize=settings.whisper.model_size,
        device=settings.whisper.device,
        computeType=settings.whisper.compute_type,
        lastError=transcriber.last_error,
    )


@app.post("/whisper/transcribe-raw", response_model=TranscriptionResponse)
async def transcribe_raw(
    request: Request,
    correlation_id: str | None = Header(default=None, alias="X-Correlation-Id"),
    filename: str | None = Header(default=None, alias="X-Filename"),
) -> TranscriptionResponse:
    body = await request.body()

    if not body:
        raise HTTPException(status_code=400, detail="Empty audio body")

    temp_dir = Path(settings.storage.temp_dir)
    temp_dir.mkdir(parents=True, exist_ok=True)

    suffix = Path(filename or "").suffix or ".wav"
    temp_file = temp_dir / f"raw_{uuid.uuid4()}{suffix}"

    try:
        temp_file.write_bytes(body)
        return transcriber.transcribe(temp_file, correlation_id)
    finally:
        try:
            temp_file.unlink(missing_ok=True)
        except Exception:
            pass


@app.post("/whisper/transcribe-upload", response_model=TranscriptionResponse)
async def transcribe_upload(
    file: UploadFile = File(...),
    correlation_id: str | None = Form(default=None, alias="correlationId"),
) -> TranscriptionResponse:
    temp_dir = Path(settings.storage.temp_dir)
    temp_dir.mkdir(parents=True, exist_ok=True)

    suffix = Path(file.filename or "").suffix or ".wav"
    temp_file = temp_dir / f"upload_{uuid.uuid4()}{suffix}"

    try:
        with temp_file.open("wb") as output:
            shutil.copyfileobj(file.file, output)

        return transcriber.transcribe(temp_file, correlation_id)
    finally:
        try:
            temp_file.unlink(missing_ok=True)
        except Exception:
            pass
