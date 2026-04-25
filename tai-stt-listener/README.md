# Tai STT Listener Microservice

## Overview

The **Tai STT Listener** is the Java microphone capture and gatekeeping module for Tai.

This second step focuses on:

- microphone capture
- silence-based auto-stop
- audio metrics
- pre-gatekeeper decision before Whisper
- final gatekeeper logic ready for Whisper integration
- debug endpoint
- health checks

It does **not** call Whisper yet.  
It does **not** publish STT callbacks to the orchestrator yet.

Next steps:

```text
Step 1: Java capture listener ✅
Step 2: Java gatekeeper ✅
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
WAV file + SpeechSegment metrics
  ↓
TranscriptGatekeeper pre-filter
```

The `TranscriptGatekeeper` already exposes both:

- `preEvaluateSegment(segment)` — before Whisper
- `evaluate(segment, transcription)` — after Whisper

---

## Exposed endpoint

### `POST /debug/mic/capture`

Captures one microphone segment and returns the generated WAV path plus audio metrics and pre-gatekeeper decision.

```bash
curl -X POST http://localhost:8094/debug/mic/capture
```

If the segment can go to Whisper, `preGatekeeperDecision` is `null`.

If the segment is rejected before Whisper:

```json
{
    "success": true,
    "segment": {
        "audioFile": "input/mic_1777168277609.wav",
        "durationMs": 3072,
        "averageEnergy": 0.4,
        "peakEnergy": 1.2,
        "voicedRatio": 0.0,
        "speechStarted": false,
        "speechEnded": false
    },
    "preGatekeeperDecision": {
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

## Gatekeeper decisions

### Pre-filter decisions

Used before Whisper to avoid unnecessary transcription.

| Reason                    | Category | Meaning                       |
|---------------------------|----------|-------------------------------|
| `SEGMENT_MISSING`         | `NOISE`  | No segment was provided       |
| `NO_SPEECH_DETECTED`      | `NOISE`  | Capture did not detect speech |
| `AUDIO_TOO_SHORT`         | `NOISE`  | Segment is too short          |
| `AUDIO_TOO_WEAK`          | `NOISE`  | Average energy is too low     |
| `NOT_ENOUGH_VOICED_AUDIO` | `NOISE`  | Voiced ratio is too low       |

### Final decisions

Ready for the next step, after Whisper integration.

| Reason                    | Category                    | Meaning                                     |
|---------------------------|-----------------------------|---------------------------------------------|
| `ACCEPTED`                | `NONE`                      | Transcript is valid                         |
| `STT_FAILED`              | `NOISE`                     | Transcription failed                        |
| `EMPTY_TRANSCRIPT`        | `NOISE` or `UNINTELLIGIBLE` | Empty transcription, depending on audio     |
| `NO_ALPHANUMERIC_CONTENT` | `NOISE`                     | Transcript contains no useful characters    |
| `UNSUPPORTED_LANGUAGE`    | `UNINTELLIGIBLE`            | Language is not allowed                     |
| `SUSPICIOUS_SEGMENT`      | `UNINTELLIGIBLE`            | Suspicion score reached rejection threshold |

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
            min-voiced-ratio: 0.15
```

### Gatekeeper properties

| Property                                                       | Description                                            |
|----------------------------------------------------------------|--------------------------------------------------------|
| `tai.stt.gatekeeper.allowed-languages`                         | Languages accepted by Tai                              |
| `tai.stt.gatekeeper.reject-audio-duration-ms`                  | Rejects very short segments                            |
| `tai.stt.gatekeeper.suspicious-audio-duration-ms`              | Adds suspicion for short but not rejected segments     |
| `tai.stt.gatekeeper.reject-average-energy-threshold`           | Rejects weak audio                                     |
| `tai.stt.gatekeeper.suspicious-language-probability-threshold` | Adds suspicion when language confidence is low         |
| `tai.stt.gatekeeper.reject-suspicion-score`                    | Suspicion score threshold for rejection                |
| `tai.stt.gatekeeper.min-voiced-ratio`                          | Minimum ratio of voiced chunks required before Whisper |

---

## Health

```http
GET /actuator/health
```

Health component:

| Component           | Meaning                                                               |
|---------------------|-----------------------------------------------------------------------|
| `microphoneCapture` | Checks whether the configured Java Sound microphone line is supported |
