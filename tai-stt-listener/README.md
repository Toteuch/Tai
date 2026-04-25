# Tai STT Listener Microservice

## Overview

The **Tai STT Listener** is the Java microphone capture and gatekeeping module for Tai.

This version connects the listener to the pure Whisper transcription service.

Current flow:

```text
MicCapture
  → PreFiltering (Gatekeeper)
  → Whisper transcription
  → PostFiltering (Gatekeeper)
  → JSON debug result
```

It still does **not** publish STT callbacks to the orchestrator. That is the next step.

---

## Architecture

```text
DebugMicController
  ↓
MicrophoneCaptureService
  ↓
SpeechSegment metrics
  ↓
TranscriptGatekeeper.preEvaluateSegment()
  ↓
HttpWhisperTranscriptionClient
  ↓
tai-stt-whisper /whisper/transcribe-upload
  ↓
TranscriptGatekeeper.evaluate()
  ↓
JSON result
```

---

## Exposed endpoint

### `POST /debug/mic/capture`

Captures one microphone segment and returns:

- captured segment metrics
- pre-gatekeeper decision
- Whisper transcription result if pre-filter accepted the segment
- final gatekeeper decision

```bash
curl -X POST "http://localhost:8094/debug/mic/capture?correlationId=test-1"
```

Example accepted response:

```json
{
  "success": true,
  "correlationId": "test-1",
  "segment": {
    "audioFile": ".\\input\\mic_1777215691832.wav",
    "durationMs": 2048,
    "averageEnergy": 21.57,
    "peakEnergy": 170.60,
    "voicedRatio": 0.125,
    "speechStarted": true,
    "speechEnded": true
  },
  "preGatekeeperDecision": null,
  "transcription": {
    "success": true,
    "correlationId": "test-1",
    "text": "Ok",
    "language": "en",
    "languageProbability": 0.85,
    "transcriptionDurationMs": 1200,
    "modelName": "small",
    "errorCode": null,
    "errorMessage": null
  },
  "finalGatekeeperDecision": {
    "accepted": true,
    "reason": "ACCEPTED",
    "suspicionScore": 0,
    "rejectionCategory": "NONE"
  }
}
```

If pre-filter rejects the segment, Whisper is skipped:

```json
{
  "preGatekeeperDecision": {
    "accepted": false,
    "reason": "NO_SPEECH_DETECTED",
    "suspicionScore": 999,
    "rejectionCategory": "NOISE"
  },
  "transcription": null,
  "finalGatekeeperDecision": {
    "accepted": false,
    "reason": "NO_SPEECH_DETECTED",
    "suspicionScore": 999,
    "rejectionCategory": "NOISE"
  }
}
```

---

## Swagger UI

```text
http://localhost:8094/docs
```

---

## Required services

Start `tai-stt-whisper` before testing the full pipeline:

```text
http://localhost:8095
```

The listener uploads the captured WAV to:

```text
POST /whisper/transcribe-upload
```

---

## Properties

```yaml
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

    gatekeeper:
      allowed-languages:
        - en
        - fr
      reject-audio-duration-ms: 250
      suspicious-audio-duration-ms: 500
      reject-average-energy-threshold: 15
      suspicious-language-probability-threshold: 0.45
      reject-suspicion-score: 2
      min-voiced-ratio: 0.10

    whisper:
      base-url: http://localhost:8095
      connect-timeout-ms: 3000
      read-timeout-ms: 120000
      transcribe-upload-path: /whisper/transcribe-upload
```

### Whisper properties

| Property | Description |
|---|---|
| `tai.stt.whisper.base-url` | Base URL of the Whisper transcription service |
| `tai.stt.whisper.connect-timeout-ms` | HTTP connection timeout |
| `tai.stt.whisper.read-timeout-ms` | HTTP read timeout for transcription |
| `tai.stt.whisper.transcribe-upload-path` | Upload endpoint used by the listener |

---

## Health

```http
GET /actuator/health
```

Current health checks still cover local microphone capture only. Whisper health will be added later if needed.
