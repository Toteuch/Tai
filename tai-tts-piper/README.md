# Tai TTS Piper Microservice

## Overview

The **Tai TTS Piper microservice** is a dedicated text-to-speech service based on Piper.

It receives a text payload, synthesizes it into a WAV file with a local Piper voice, plays the WAV on the local audio output, and publishes playback callbacks.

```text
text
  → Piper synthesis
  → WAV file
  → local audio playback
  → playback callback
```

The service is self-contained for its Piper runtime:

```text
tai-tts-piper/
  .venv/
  voices/
  output/
  src/
  pom.xml
```

---

## Architecture

```text
TtsController
  ↓
TtsPlaybackService (@Async)
  ↓
PiperSynthesisService
  ↓
Piper executable
  ↓
JavaAudioPlaybackService
  ↓
OrchestratorTtsEventClient
```

The `/tts/speak` endpoint returns quickly. Synthesis and playback are handled asynchronously.

---

## Main components

| Component | Role |
|---|---|
| `TtsController` | Exposes speech and stop endpoints |
| `TtsPlaybackService` | Orchestrates asynchronous synthesis, playback and callback publication |
| `PiperSynthesisService` | Calls the local Piper executable and generates a WAV file |
| `JavaAudioPlaybackService` | Plays the generated WAV file through local audio output |
| `OrchestratorTtsEventClient` | Publishes TTS playback callbacks |
| `PiperHealthIndicator` | Checks Piper executable, model and config files |
| `PlaybackHealthIndicator` | Reports active playback state |

---

## Endpoints

### `POST /tts/speak`

Starts speech synthesis and playback.

Request:

```json
{
  "correlationId": "test-1",
  "text": "Hello, I am Tai."
}
```

Example:

```bash
curl -X POST http://localhost:8093/tts/speak \
  -H "Content-Type: application/json" \
  -d '{"correlationId":"test-1","text":"Hello, I am Tai."}'
```

The request is accepted immediately. The service then synthesizes and plays speech asynchronously.

---

### `POST /tts/stop`

Stops active playback when the request matches the active playback correlation id.

Request:

```json
{
  "correlationId": "test-1"
}
```

Example:

```bash
curl -X POST http://localhost:8093/tts/stop \
  -H "Content-Type: application/json" \
  -d '{"correlationId":"test-1"}'
```

---

## Swagger UI

```text
http://localhost:8093/docs
```

OpenAPI JSON:

```text
http://localhost:8093/v3/api-docs
```

---

## Playback callbacks

The service publishes playback callbacks to the configured callback target.

### Playback started

```http
POST /events/tts/playback-started
```

Sent after synthesis succeeds and playback is about to start.

Payload fields include:

- `eventId`
- `createdAt`
- `source`
- `correlationId`
- `voiceId`
- `synthesisDurationMs`

---

### Playback completed

```http
POST /events/tts/playback-completed
```

Sent when playback completes normally.

Payload fields include:

- `eventId`
- `createdAt`
- `source`
- `correlationId`
- `voiceId`
- `speechDurationMs`

---

### Playback failed

```http
POST /events/tts/playback-failed
```

Sent when synthesis or playback fails.

Payload fields include:

- `eventId`
- `createdAt`
- `source`
- `correlationId`
- `voiceId`
- `errorCode`
- `errorMessage`
- `speechDurationMs`

---

## Business flows

### Nominal playback

```text
POST /tts/speak
  → request accepted
  → Piper generates WAV
  → playback-started callback
  → WAV playback
  → playback-completed callback
  → generated WAV cleanup
```

---

### Stop playback

```text
POST /tts/stop
  → active playback correlation id checked
  → playback stopped when it matches
  → normal completion callback suppressed for stopped playback
```

---

### Failure

```text
POST /tts/speak
  → synthesis or playback fails
  → playback-failed callback
```

---

## Generated WAV files

Piper writes generated WAV files into the configured output directory.

Current default:

```text
./output
```

Played WAV files are deleted after playback so the output directory does not grow indefinitely.

---

## Configuration

```yaml
server:
  port: 8093

tai:
  tts:
    piper:
      executable: ./.venv/Scripts/piper.exe
      model: ./voices/en_GB-alba-medium.onnx
      config: ./voices/en_GB-alba-medium.onnx.json
      output-dir: ./output
      voice-id: en_GB-alba-medium
      process-timeout-ms: 60000

    orchestrator:
      base-url: http://localhost:8080
      connect-timeout-ms: 3000
      read-timeout-ms: 10000
      callbacks:
        playback-started-path: /events/tts/playback-started
        playback-completed-path: /events/tts/playback-completed
        playback-failed-path: /events/tts/playback-failed
```

### Piper properties

| Property | Description |
|---|---|
| `tai.tts.piper.executable` | Local Piper executable path |
| `tai.tts.piper.model` | Piper ONNX voice model path |
| `tai.tts.piper.config` | Piper voice JSON config path |
| `tai.tts.piper.output-dir` | Generated WAV output directory |
| `tai.tts.piper.voice-id` | Logical voice id included in callbacks |
| `tai.tts.piper.process-timeout-ms` | Maximum synthesis process duration before failure |

### Callback properties

| Property | Description |
|---|---|
| `tai.tts.orchestrator.base-url` | Base URL used for TTS callbacks |
| `tai.tts.orchestrator.connect-timeout-ms` | HTTP connection timeout for callbacks |
| `tai.tts.orchestrator.read-timeout-ms` | HTTP read timeout for callbacks |
| `tai.tts.orchestrator.callbacks.playback-started-path` | Callback path for playback-started events |
| `tai.tts.orchestrator.callbacks.playback-completed-path` | Callback path for playback-completed events |
| `tai.tts.orchestrator.callbacks.playback-failed-path` | Callback path for playback-failed events |

---

## Health

```http
GET /actuator/health
```

Health components:

| Component | Meaning |
|---|---|
| `piper` | Checks Piper executable, model and config files |
| `playback` | Reports active playback state |

Example:

```bash
curl http://localhost:8093/actuator/health
```

A typical playback component can expose the active playback correlation id:

```json
{
  "components": {
    "playback": {
      "status": "UP",
      "details": {
        "activeCorrelationId": "c9540aec-c84c-4051-9467-5701c46d095c"
      }
    }
  },
  "status": "UP"
}
```

---

## Orchestrator UI integration

The TTS service does not own UI state directly.

Its playback callbacks update the orchestrator runtime registry:

| Callback | UI runtime effect |
|---|---|
| `playback-started` | `TTS_PIPER` becomes `SPEAKING` |
| `playback-completed` | `TTS_PIPER` becomes `IDLE` / `Silent` |
| `playback-failed` | `TTS_PIPER` becomes `DEGRADED` / `Error` |

The orchestrator also refreshes the TTS Actuator health endpoint asynchronously. Health checks update module health and details, but they do not block live UI snapshot publication.

The V2 UI Stop Speak control is planned on the orchestrator side. The TTS service already exposes `POST /tts/stop`, which the orchestrator uses for barge-in when it needs to stop active playback.
