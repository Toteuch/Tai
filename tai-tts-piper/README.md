# Tai TTS Piper Microservice

## Overview

The **Tai TTS Piper microservice** generates and plays speech using Piper.

It is autonomous: Piper runtime, voices and generated WAV files live inside this module.

```text
Tai Orchestrator
    → HTTP TtsClient
    → Tai TTS Piper microservice
    → local Piper executable
    → local voices
    → WAV playback
    → callback to Orchestrator
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
  ↓
POST /events/tts/*
```

## Exposed endpoints

### `POST /tts/speak`

```json
{
  "correlationId": "test-1",
  "text": "Hello, I am Tai."
}
```

### `POST /tts/stop`

```json
{
  "correlationId": "test-1"
}
```

## Swagger UI

```text
http://localhost:8093/docs
```

## Generated events

- `POST /events/tts/playback-started`
- `POST /events/tts/playback-completed`
- `POST /events/tts/playback-failed`

All callbacks include:

- `eventId`
- `createdAt`
- `source = TTS_SERVICE`
- `correlationId`

## Business flows

### Nominal playback

```text
1. Orchestrator calls POST /tts/speak
2. TTS service accepts immediately
3. Piper generates a WAV file
4. TTS service sends playback-started
5. TTS service plays WAV
6. TTS service sends playback-completed
```

### Stop / barge-in

```text
1. Orchestrator calls POST /tts/stop
2. TTS service stops active playback if correlationId matches
3. Completion callback is suppressed for the stopped playback
```

## Properties

```yaml
tai:
  tts:
    piper:
      executable: ./.venv/Scripts/piper.exe
      model: ./voices/en_GB-alba-medium.onnx
      config: ./voices/en_GB-alba-medium.onnx.json
      output-dir: ./output
      voice-id: en_GB-alba-medium
      process-timeout-ms: 60000
```

| Property | Description |
|---|---|
| `tai.tts.piper.executable` | Piper executable inside this module |
| `tai.tts.piper.model` | Piper ONNX voice model inside `voices/` |
| `tai.tts.piper.config` | Piper voice JSON config inside `voices/` |
| `tai.tts.piper.output-dir` | Generated WAV output directory |
| `tai.tts.piper.voice-id` | Logical voice id sent to the orchestrator |
| `tai.tts.piper.process-timeout-ms` | Max synthesis duration before failure |

## Health

```http
GET /actuator/health
```

Health components:

| Component | Meaning |
|---|---|
| `piper` | Checks Piper executable, model and config files |
| `playback` | Reports active playback correlation id |
