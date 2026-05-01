# Tai STT Listener Microservice

## Overview

The **Tai STT Listener** is the Java service responsible for microphone capture, speech segmentation, STT gatekeeping and STT event publication.

It supports two usage modes:

```text
continuous listening
  → microphone stays open
  → speech segments are detected automatically
  → STT pipeline runs for each segment
  → STT events can be published

debug capture
  → one request captures one segment
  → STT pipeline runs once
  → JSON response is returned
```

The service delegates pure Whisper transcription to an external HTTP transcription service through the configured `tai.stt.whisper.*` properties.

---

## Architecture

```text
ListenerController
  ↓
ContinuousListeningService
  ↓
SpeechSegmentRecorder
  ↓
SttPipelineService
  ↓
TranscriptGatekeeper.preEvaluateSegment()
  ↓
HttpWhisperTranscriptionClient
  ↓
TranscriptGatekeeper.evaluate()
  ↓
OrchestratorSttEventClient
```

Debug flow:

```text
DebugMicController
  ↓
MicrophoneCaptureService
  ↓
SttPipelineService
  ↓
JSON debug response
```

---

## Main components

| Component | Role |
|---|---|
| `ListenerController` | Exposes continuous listener start/stop endpoints |
| `ContinuousListeningService` | Owns continuous microphone listening lifecycle |
| `SpeechSegmentRecorder` | Records bounded speech segments from an open microphone line |
| `DebugMicController` | Exposes request/response debug capture endpoints |
| `MicrophoneCaptureService` | Captures one microphone segment for debug mode |
| `SttPipelineService` | Runs pre-filtering, transcription, post-filtering and cleanup |
| `SpeechSegment` | Contains captured WAV path and audio metrics |
| `TranscriptGatekeeper` | Applies pre-filtering and final transcript decisions |
| `HttpWhisperTranscriptionClient` | Sends raw WAV bytes to the configured Whisper service |
| `OrchestratorSttEventClient` | Publishes STT callback payloads |
| `CaptureHealthIndicator` | Checks Java Sound microphone compatibility |
| `ContinuousListenerHealthIndicator` | Reports continuous listener runtime state through Actuator |

---

## Continuous listener mode

The continuous listener keeps the microphone open and processes successive utterances.

Runtime flow:

```text
start listener
  → open microphone line
  → wait for speech
  → detect speech start
  → publish speech-started callback if enabled
  → record current utterance
  → detect speech end after configured silence
  → write WAV segment
  → run STT pipeline
  → publish final STT callback if enabled
  → cleanup WAV if enabled
  → return to WAITING_FOR_SPEECH
```

Control endpoints:

```http
POST /listener/start
POST /listener/stop
```

Runtime status is exposed through:

```http
GET /actuator/health
```

There is no dedicated `GET /listener/status` endpoint.

---

## Listener states

```text
STOPPED
STARTING
WAITING_FOR_SPEECH
CAPTURING
PROCESSING
ERROR
```

Typical transition:

```text
STOPPED
  → STARTING
  → WAITING_FOR_SPEECH
  → CAPTURING
  → PROCESSING
  → WAITING_FOR_SPEECH
```

---

## STT pipeline

The STT pipeline is shared by continuous mode and debug mode.

```text
SpeechSegment
  → pre-gatekeeper
  → Whisper transcription if accepted
  → final gatekeeper
  → cleanup captured WAV if enabled
  → SttPipelineResult
```

If the pre-gatekeeper rejects the segment, Whisper is skipped.

---

## Endpoints

### `POST /listener/start`

Starts continuous microphone listening.

```bash
curl -X POST "http://localhost:8094/listener/start"
```

Example response:

```json
{
  "running": true,
  "state": "STARTING",
  "activeCorrelationId": null,
  "lastSegmentAt": null,
  "lastResult": null,
  "lastError": null
}
```

---

### `POST /listener/stop`

Stops continuous microphone listening and closes the microphone line.

```bash
curl -X POST "http://localhost:8094/listener/stop"
```

Example response:

```json
{
  "running": false,
  "state": "STOPPED",
  "activeCorrelationId": null,
  "lastSegmentAt": "2026-04-27T18:40:15.123Z",
  "lastResult": {
    "accepted": true,
    "reason": "ACCEPTED",
    "rejectionCategory": "NONE",
    "text": "Hello Tai",
    "language": "en",
    "completedAt": "2026-04-27T18:40:15.123Z"
  },
  "lastError": null
}
```

---

### `POST /debug/mic/capture`

Captures one microphone segment and runs the STT pipeline without publishing STT callbacks.

```bash
curl -X POST "http://localhost:8094/debug/mic/capture?correlationId=test-1"
```

---

### `POST /debug/mic/capture-and-callback`

Captures one microphone segment, runs the STT pipeline and publishes the final STT callback.

```bash
curl -X POST "http://localhost:8094/debug/mic/capture-and-callback?correlationId=test-1"
```

This endpoint does not publish `speechStarted`.

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

## Debug response model

### Accepted response

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
    "transcriptionDurationMs": 390,
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

Captured WAV files can be deleted after processing. In that case, `audioFile` identifies the file used during processing, but the file may no longer exist when the response is read.

### Pre-filter rejection

```json
{
  "success": true,
  "correlationId": "test-1",
  "segment": {
    "audioFile": ".\\input\\mic_1777215691832.wav",
    "durationMs": 3072,
    "averageEnergy": 0.47,
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

## Whisper HTTP contract

The listener sends captured WAV bytes to the configured raw transcription endpoint.

Default endpoint:

```text
POST http://localhost:8095/whisper/transcribe-raw
```

Request:

```text
Content-Type: audio/wav
Body: raw WAV bytes
X-Correlation-Id: <correlationId>
X-Filename: <captured filename>
```

The Java HTTP client uses HTTP/1.1 for this call.

---

## STT callback contracts

The callback base URL and paths are configured through `tai.stt.orchestrator.*`.

### Speech started

Sent when continuous mode detects speech start.

```http
POST /events/stt/speech-started
```

Payload:

```json
{
  "eventId": "...",
  "createdAt": "2026-04-27T18:40:15.123Z",
  "source": "STT_SERVICE",
  "correlationId": "test-1",
  "averageEnergy": 132.22,
  "peakEnergy": 643.66
}
```

The same `correlationId` is reused for the final STT callback of the same utterance.

---

### Accepted transcript

Sent when the final gatekeeper accepts the transcript.

```http
POST /events/stt/transcript-accepted
```

Payload:

```json
{
    "eventId": "...",
    "occurredAt": "2026-04-27T18:40:15.123Z",
    "source": "STT_SERVICE",
    "correlationId": "test-1",
    "transcript": "Hello Tai",
    "language": "en",
    "languageProbability": 0.85,
    "userSpeechDurationMs": 3200,
    "transcriptionDurationMs": 390
}
```

---

### Unintelligible transcript

Sent when the final gatekeeper rejects the segment as unintelligible.

```http
POST /events/stt/transcript-unintelligible
```

Typical reasons:

- `EMPTY_TRANSCRIPT`
- `UNSUPPORTED_LANGUAGE`
- `SUSPICIOUS_SEGMENT`

---

### Noise

Sent when the final gatekeeper rejects the segment as noise.

```http
POST /events/stt/transcript-noise
```

Typical reasons:

- `NO_SPEECH_DETECTED`
- `AUDIO_TOO_SHORT`
- `AUDIO_TOO_WEAK`
- `NOT_ENOUGH_VOICED_AUDIO`
- `STT_FAILED`
- `NO_ALPHANUMERIC_CONTENT`

---

## Gatekeeper decisions

### Pre-filter decisions

Used before Whisper transcription.

| Reason | Category | Meaning |
|---|---|---|
| `SEGMENT_MISSING` | `NOISE` | No segment was provided |
| `NO_SPEECH_DETECTED` | `NOISE` | Capture did not detect speech |
| `AUDIO_TOO_SHORT` | `NOISE` | Segment is too short |
| `AUDIO_TOO_WEAK` | `NOISE` | Audio peak/energy is too weak |
| `NOT_ENOUGH_VOICED_AUDIO` | `NOISE` | Voiced ratio is too low |

### Final decisions

Used after Whisper transcription.

| Reason | Category | Meaning |
|---|---|---|
| `ACCEPTED` | `NONE` | Transcript is valid |
| `STT_FAILED` | `NOISE` | Whisper call or transcription failed |
| `EMPTY_TRANSCRIPT` | `NOISE` or `UNINTELLIGIBLE` | Empty transcription, depending on audio context |
| `NO_ALPHANUMERIC_CONTENT` | `NOISE` | Transcript contains no useful alphanumeric content |
| `UNSUPPORTED_LANGUAGE` | `UNINTELLIGIBLE` | Language is not in the allowed language list |
| `SUSPICIOUS_SEGMENT` | `UNINTELLIGIBLE` | Suspicion score reached rejection threshold |

---

## Configuration

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

    listener:
      auto-start: false
      continue-on-error: true
      delete-audio-after-processing: true
      publish-speech-started-callbacks: true
      publish-final-callbacks: true

    gatekeeper:
      allowed-languages:
        - en
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
      transcribe-raw-path: /whisper/transcribe-raw

    orchestrator:
      base-url: http://localhost:8080
      connect-timeout-ms: 3000
      read-timeout-ms: 10000
      callbacks:
        speech-started-path: /events/stt/speech-started
        transcript-accepted-path: /events/stt/transcript-accepted
        transcript-unintelligible-path: /events/stt/transcript-unintelligible
        transcript-noise-path: /events/stt/transcript-noise
```

### Capture properties

| Property | Description |
|---|---|
| `tai.stt.capture.output-dir` | Directory where captured WAV files are temporarily written |
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
| `tai.stt.capture.no-speech-timeout-ms` | Early stop timeout used by debug capture mode |

### Listener properties

| Property | Description |
|---|---|
| `tai.stt.listener.auto-start` | Starts continuous listening automatically when the service starts |
| `tai.stt.listener.continue-on-error` | Keeps the listening loop alive after recoverable processing errors |
| `tai.stt.listener.delete-audio-after-processing` | Deletes captured WAV files after STT processing |
| `tai.stt.listener.publish-speech-started-callbacks` | Publishes the speech-started callback on speech detection |
| `tai.stt.listener.publish-final-callbacks` | Publishes final transcript/noise/unintelligible callbacks after processing |

### Gatekeeper properties

| Property | Description |
|---|---|
| `tai.stt.gatekeeper.allowed-languages` | Languages accepted after transcription |
| `tai.stt.gatekeeper.reject-audio-duration-ms` | Rejects very short segments |
| `tai.stt.gatekeeper.suspicious-audio-duration-ms` | Adds suspicion for short but not rejected segments |
| `tai.stt.gatekeeper.reject-average-energy-threshold` | Rejects weak audio |
| `tai.stt.gatekeeper.suspicious-language-probability-threshold` | Adds suspicion when language confidence is low |
| `tai.stt.gatekeeper.reject-suspicion-score` | Suspicion score threshold for rejection |
| `tai.stt.gatekeeper.min-voiced-ratio` | Minimum ratio of voiced chunks required before Whisper |

### Whisper properties

| Property | Description |
|---|---|
| `tai.stt.whisper.base-url` | Base URL of the transcription service |
| `tai.stt.whisper.connect-timeout-ms` | HTTP connection timeout |
| `tai.stt.whisper.read-timeout-ms` | HTTP read timeout for transcription |
| `tai.stt.whisper.transcribe-raw-path` | Raw WAV endpoint used by the listener |

### Callback properties

| Property | Description |
|---|---|
| `tai.stt.orchestrator.base-url` | Base URL used for STT callbacks |
| `tai.stt.orchestrator.connect-timeout-ms` | HTTP connection timeout for callbacks |
| `tai.stt.orchestrator.read-timeout-ms` | HTTP read timeout for callbacks |
| `tai.stt.orchestrator.callbacks.speech-started-path` | Callback path for speech-started events |
| `tai.stt.orchestrator.callbacks.transcript-accepted-path` | Callback path for accepted transcript events |
| `tai.stt.orchestrator.callbacks.transcript-unintelligible-path` | Callback path for unintelligible transcript events |
| `tai.stt.orchestrator.callbacks.transcript-noise-path` | Callback path for noise events |

---

## Health

```http
GET /actuator/health
```

Health components:

| Component | Meaning |
|---|---|
| `microphoneCapture` | Checks whether the configured Java Sound microphone line is supported |
| `continuousListener` | Reports listener loop state and latest runtime details |

Expected `continuousListener` details:

- `running`
- `state`
- `activeCorrelationId`
- `lastSegmentAt`
- `lastDecision`
- `lastTranscript`
- `lastError`
- `autoStart`
- `publishSpeechStartedCallbacks`
- `publishFinalCallbacks`
