# Tai STT Listener Microservice

## Overview

The **Tai STT Listener** is the Java microphone capture module for Tai.

This first step intentionally focuses on the responsibility that should clearly live in Java:

- microphone capture
- silence-based auto-stop
- audio metrics
- debug endpoint
- health checks

It does **not** perform Whisper transcription yet.  
It does **not** publish STT callbacks to the orchestrator yet.

Those responsibilities will be added in the next steps:

```text
Step 1: Java capture listener
Step 2: Java gatekeeper
Step 3: Whisper transcription service
Step 4: Orchestrator callbacks
```

---

## Architecture

```text
DebugMicController
  ↓
MicrophoneCaptureService
  ↓
Java Sound TargetDataLine
  ↓
WAV file
  ↓
SpeechSegment metrics
```

---

## Exposed endpoint

### `POST /debug/mic/capture`

Captures one microphone segment and returns the generated WAV path plus audio metrics.

```bash
curl -X POST http://localhost:8094/debug/mic/capture
```

Example response:

```json
{
  "success": true,
  "segment": {
    "audioFile": "input/mic_1777168277609.wav",
    "durationMs": 2560,
    "averageEnergy": 120.8,
    "peakEnergy": 540.2,
    "voicedRatio": 0.42,
    "speechStarted": true,
    "speechEnded": true
  }
}
```

---

## Swagger UI

```text
http://localhost:8094/docs
```

OpenAPI JSON:

```text
http://localhost:8094/v3/api-docs
```

---

## Properties

```yaml
server:
  port: 8094

tai:
  stt:
    capture:
      output-dir: ./input
      sample-rate: 16000
      sample-size-bits: 16
      channels: 1
      signed: true
      big-endian: false
      buffer-size: 4096
      silence-threshold: 40
      silence-duration-ms: 1200
      min-recording-ms: 800
      max-recording-ms: 15000
      no-speech-timeout-ms: 3000
```

| Property | Description |
|---|---|
| `server.port` | HTTP port of the listener service |
| `tai.stt.capture.output-dir` | Directory where captured WAV files are written |
| `tai.stt.capture.sample-rate` | Audio sample rate |
| `tai.stt.capture.sample-size-bits` | Sample size in bits |
| `tai.stt.capture.channels` | Number of audio channels |
| `tai.stt.capture.signed` | Whether PCM samples are signed |
| `tai.stt.capture.big-endian` | Whether samples are big-endian |
| `tai.stt.capture.buffer-size` | Audio buffer size used by Java Sound |
| `tai.stt.capture.silence-threshold` | Energy threshold used to detect speech |
| `tai.stt.capture.silence-duration-ms` | Silence duration required to stop after speech |
| `tai.stt.capture.min-recording-ms` | Minimum recording time before silence can stop the segment |
| `tai.stt.capture.max-recording-ms` | Hard maximum recording duration |
| `tai.stt.capture.no-speech-timeout-ms` | Early stop timeout when no speech is detected |

---

## Health

Spring Boot Actuator is enabled.

```http
GET /actuator/health
```

Health component:

| Component | Meaning |
|---|---|
| `microphoneCapture` | Checks whether the configured Java Sound microphone line is supported |

---

## Run locally

```bash
mvn spring-boot:run
```

Then open:

```text
http://localhost:8094/docs
```
