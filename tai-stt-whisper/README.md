# Tai STT Whisper Microservice

## Overview

The **Tai STT Whisper microservice** is a dedicated speech-to-text transcription service.

It receives WAV audio, runs `faster-whisper`, and returns a normalized transcription response.

```text
WAV audio
  → faster-whisper
  → transcription result
```

The service is intentionally focused on transcription only.

---

## Architecture

```text
FastAPI controller
  ↓
WhisperTranscriber
  ↓
faster-whisper model
  ↓
TranscriptionResponse
```

The Whisper model is loaded once when the service starts and stays loaded while the process is alive.

Current runtime profile:

```text
model-size: small
device: cuda
compute-type: int8_float16
language: en
```

---

## Endpoints

### `GET /health`

Returns service and model status.

```bash
curl http://localhost:8095/health
```

Example:

```json
{
  "status": "UP",
  "modelLoaded": true,
  "modelSize": "small",
  "device": "cuda",
  "computeType": "int8_float16",
  "lastError": null
}
```

---

### `POST /whisper/transcribe-raw`

Transcribes raw WAV bytes.

This endpoint is used by service-to-service callers.

Request:

```text
Content-Type: audio/wav
Body: raw WAV bytes
X-Correlation-Id: <correlationId>
X-Filename: <filename>
```

Example:

```bash
curl -X POST http://localhost:8095/whisper/transcribe-raw \
  -H "Content-Type: audio/wav" \
  -H "X-Correlation-Id: test-1" \
  -H "X-Filename: mic.wav" \
  --data-binary "@./input/mic.wav"
```

---

### `POST /whisper/transcribe-upload`

Transcribes an uploaded WAV file.

This endpoint is mainly useful for manual testing through Swagger UI.

Fields:

| Field | Required | Description |
|---|---:|---|
| `file` | yes | WAV file to transcribe |
| `correlationId` | no | Correlation id copied to the response |

Example:

```bash
curl -X POST http://localhost:8095/whisper/transcribe-upload \
  -F "correlationId=test-1" \
  -F "file=@./input/mic.wav;type=audio/wav"
```

---

## Swagger UI

```text
http://localhost:8095/docs
```

OpenAPI JSON:

```text
http://localhost:8095/openapi.json
```

---

## Response model

### Success

```json
{
  "success": true,
  "correlationId": "test-1",
  "text": "Hello Tai, how are you doing?",
  "language": "en",
  "languageProbability": 0.98,
  "transcriptionDurationMs": 390,
  "modelName": "small",
  "errorCode": null,
  "errorMessage": null
}
```

### Failure

```json
{
  "success": false,
  "correlationId": "test-1",
  "text": null,
  "language": null,
  "languageProbability": null,
  "transcriptionDurationMs": 12,
  "modelName": "small",
  "errorCode": "WHISPER_TRANSCRIPTION_ERROR",
  "errorMessage": "..."
}
```

---

## Configuration

Configuration lives in:

```text
config.yaml
```

Current configuration:

```yaml
server:
  host: 127.0.0.1
  port: 8095

whisper:
  model-size: small
  device: cuda
  compute-type: int8_float16
  beam-size: 5
  temperature: 0.0
  condition-on-previous-text: false
  vad-filter: false
  language: en
  initial-prompt: "The assistant is named Tai. Expected language is English. Common words: Tai, LLM, TTS, STT."

storage:
  temp-dir: ./tmp
```

| Property | Description |
|---|---|
| `server.host` | Host used by Uvicorn |
| `server.port` | Port used by Uvicorn |
| `whisper.model-size` | Whisper model size loaded at startup |
| `whisper.device` | Runtime device: `cuda` or `cpu` |
| `whisper.compute-type` | Quantization/precision mode used by faster-whisper |
| `whisper.beam-size` | Beam search size used during transcription |
| `whisper.temperature` | Whisper decoding temperature |
| `whisper.condition-on-previous-text` | Whether Whisper conditions on previous text |
| `whisper.vad-filter` | faster-whisper VAD option |
| `whisper.language` | Forced transcription language |
| `whisper.initial-prompt` | Prompt used to bias transcription vocabulary and context |
| `storage.temp-dir` | Temporary directory used while processing uploaded/raw files |

---

## Dependencies

The service depends on:

- FastAPI
- Uvicorn
- Pydantic
- faster-whisper
- NVIDIA CUDA/cuDNN Python packages for GPU execution

Current GPU dependency packages:

```text
nvidia-cublas-cu12
nvidia-cudnn-cu12
```

---

## Performance notes

The response includes:

```text
transcriptionDurationMs
```

This metric measures the transcription time inside this service.

With the current GPU profile:

```text
small + cuda + int8_float16 + initial prompt
```

short utterances are typically transcribed in a few hundred milliseconds on the local RTX GPU setup.

