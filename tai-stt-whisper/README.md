# Tai STT Whisper Microservice

## Overview

The **Tai STT Whisper microservice** is a pure transcription service.

It intentionally does only one thing:

```text
audio file / uploaded WAV
  → Whisper
  → transcription result
```

It does **not**:

- capture microphone audio
- apply gatekeeper rules
- publish callbacks to the orchestrator
- know anything about Tai event flows

Those responsibilities stay in `tai-stt-listener`.

---

## Architecture

```text
FastAPI controller
  ↓
WhisperTranscriber
  ↓
faster-whisper model loaded once at startup
  ↓
TranscriptionResponse
```

The Whisper model is loaded once when the service starts and stays loaded while the process is alive.

---

## Exposed endpoints

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
    "device": "cpu",
    "computeType": "int8",
    "lastError": null
}
```

---

### `POST /whisper/transcribe-file`

Transcribes a file path accessible from the Whisper service.

This is useful for local debug when the caller and the service share the same filesystem.

```json
{
    "correlationId": "test-1",
    "audioFile": ".../Tai/tai-stt-listener/input/mic.wav"
}
```

---

### `POST /whisper/transcribe-upload`

Transcribes an uploaded audio file.

This is the preferred service-to-service contract because it does not require shared filesystem paths.

Fields:

- `file`: WAV file
- `correlationId`: optional correlation id

---

## Swagger UI

FastAPI exposes Swagger UI at:

```text
http://localhost:8095/docs
```

OpenAPI JSON:

```text
http://localhost:8095/openapi.json
```

---

## Response model

```json
{
    "success": true,
    "correlationId": "test-1",
    "text": "Bonjour Tai",
    "language": "fr",
    "languageProbability": 0.98,
    "transcriptionDurationMs": 1234,
    "modelName": "small",
    "errorCode": null,
    "errorMessage": null
}
```

---

## Properties

Configuration lives in:

```text
config.yaml
```

```yaml
server:
    host: 127.0.0.1
    port: 8095

whisper:
    model-size: small
    device: cpu
    compute-type: int8
    beam-size: 5
    vad-filter: false

storage:
    temp-dir: ./tmp
```

| Property               | Description                                            |
|------------------------|--------------------------------------------------------|
| `server.host`          | Host used when launching with uvicorn                  |
| `server.port`          | Port used when launching with uvicorn                  |
| `whisper.model-size`   | Whisper model size loaded at startup                   |
| `whisper.device`       | Runtime device, usually `cpu` or `cuda`                |
| `whisper.compute-type` | Quantization/precision mode, for example `int8` on CPU |
| `whisper.beam-size`    | Beam search size used during transcription             |
| `whisper.vad-filter`   | Faster-whisper VAD filter option                       |
| `storage.temp-dir`     | Temporary directory for uploaded files                 |

---

## Business flow with listener

Target integration:

```text
1. tai-stt-listener captures microphone audio
2. tai-stt-listener pre-gatekeeper accepts the segment
3. tai-stt-listener uploads the WAV to tai-stt-whisper
4. tai-stt-whisper returns the transcript
5. tai-stt-listener applies the final gatekeeper
6. tai-stt-listener publishes the STT callback to the orchestrator
```
