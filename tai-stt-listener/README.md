# Tai STT Listener Microservice

## Overview

The **Tai STT Listener** is the Java microphone capture, gatekeeping and STT callback module for Tai.

It owns the Java-side STT flow:

```text
MicCapture
  → PreFiltering (Gatekeeper)
  → Whisper transcription
  → PostFiltering (Gatekeeper)
  → optional Orchestrator callback
  → JSON debug result
```

The listener does **not** run Whisper itself. Pure transcription is delegated to `tai-stt-whisper`.

The listener is responsible for:

- microphone capture
- silence-based auto-stop
- audio metrics extraction
- pre-filtering before Whisper
- raw WAV call to `tai-stt-whisper`
- post-filtering after Whisper
- STT callbacks to the orchestrator
- cleanup of captured WAV files after processing

---

## Current architecture

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
tai-stt-whisper /whisper/transcribe-raw
  ↓
TranscriptGatekeeper.evaluate()
  ↓
OrchestratorSttEventClient optional callback
  ↓
JSON debug result
```

### Main components

| Component | Role |
|---|---|
| `DebugMicController` | Exposes debug endpoints for capture and capture+callback |
| `MicrophoneCaptureService` | Captures one microphone segment using Java Sound |
| `SpeechSegment` | Contains WAV path and audio metrics |
| `TranscriptGatekeeper` | Applies pre-filtering and post-filtering decisions |
| `HttpWhisperTranscriptionClient` | Sends raw WAV bytes to `tai-stt-whisper` |
| `OrchestratorSttEventClient` | Sends STT callbacks to the orchestrator |
| `CaptureHealthIndicator` | Checks Java Sound microphone compatibility |

---

## Target architecture: continuous listener mode

The next implementation step is to add a **continuous microphone listener**.

The goal is not to publish a `speechStarted` event yet.
The first goal is to make the listener reliably keep the microphone open, detect user speech, bound one utterance, process it, and return to waiting for the next utterance.

Target runtime flow:

```text
tai-stt-listener starts
  → opens microphone line
  → waits for speech
  → detects speech start internally
  → records the current utterance
  → detects speech end after configured silence
  → writes WAV segment
  → runs existing STT pipeline:
      PreFiltering
      Whisper transcription
      PostFiltering
  → stores/logs latest result
  → returns to WAITING_FOR_SPEECH
```

This step should not require changes in `tai-orchestrator`.

Orchestrator impact:

| Feature | Orchestrator impact |
|---|---|
| Continuous microphone open | None |
| Speech segmentation inside listener | None |
| Reusing existing final STT callbacks | None if existing callback contracts stay unchanged |
| Continuous listener health reporting | None at runtime; later consumed by health consolidation |
| Future `speechStarted` event for barge-in | Yes, later step |
| Future orchestrator handling of `speechStarted` | Yes, later step |

The current decoupling allows the listener runtime to evolve internally while the orchestrator keeps receiving the same final STT events.

---

## Planned continuous listener components

### `ContinuousListeningService`

Owns the continuous listener lifecycle.

Responsibilities:

- start the listening loop
- stop the listening loop
- expose runtime status internally to health indicators
- prevent duplicate loops
- keep latest result and latest error
- own the listener state transitions

Expected methods:

```java
start()
stop()
status()
```

`status()` is not exposed as a dedicated REST endpoint. It is used by Actuator health.

---

### `ListeningState`

Tracks the current runtime state.

```java
STOPPED
STARTING
WAITING_FOR_SPEECH
CAPTURING
PROCESSING
ERROR
```

This state will be useful for:

- logs
- Actuator health details
- future UI
- debugging continuous capture behavior

---

### `ContinuousListenerHealthIndicator`

Exposes continuous listener runtime state through Actuator.

Target endpoint:

```http
GET /actuator/health
```

Target health component:

```json
{
  "status": "UP",
  "components": {
    "continuousListener": {
      "status": "UP",
      "details": {
        "running": true,
        "state": "WAITING_FOR_SPEECH",
        "activeCorrelationId": null,
        "lastSegmentAt": "2026-04-26T18:40:15.123Z",
        "lastDecision": "ACCEPTED",
        "lastTranscript": "Hello Tai",
        "lastError": null
      }
    }
  }
}
```

There is intentionally no `GET /listener/status` endpoint for V1. Runtime state belongs in health because it prepares the future orchestrator-level health consolidation.

Recommended health mapping:

| Listener state | Health status |
|---|---|
| `WAITING_FOR_SPEECH` | `UP` |
| `CAPTURING` | `UP` |
| `PROCESSING` | `UP` |
| `STOPPED` with `auto-start=false` | `UP` |
| `STOPPED` with `auto-start=true` | `OUT_OF_SERVICE` or `DOWN` |
| `ERROR` | `DOWN` or `DEGRADED` |

The exact mapping can be adjusted during implementation, but `STOPPED` should not automatically mean `DOWN` when manual start is expected.

---

### `SpeechSegmentRecorder`

Records one speech segment from an already opened microphone line.

Unlike `MicrophoneCaptureService.captureOnce()`, it should not open and close the microphone for each capture.

Expected behavior:

```text
use existing open TargetDataLine
  → wait until energy crosses speech threshold
  → capture while speech continues
  → stop segment after configured silence
  → return SpeechSegment
```

---

### `SttPipelineService`

Extracts the current pipeline logic out of the controller so it can be reused by both debug endpoints and the continuous listener.

Target API:

```java
SttPipelineResult process(SpeechSegment segment, String correlationId)
```

Responsibilities:

- pre-filter with `TranscriptGatekeeper`
- skip Whisper if pre-filter rejects the segment
- call `tai-stt-whisper` when needed
- post-filter with `TranscriptGatekeeper`
- cleanup captured WAV files
- return a reusable pipeline result

---

## Continuous listener development plan

### Step 1 — Extract reusable STT pipeline

Move the shared logic currently used by `DebugMicController` into `SttPipelineService`.

Current controller logic:

```text
capture
  → preGatekeeper
  → whisper
  → postGatekeeper
  → cleanup
  → response
```

Target:

```text
DebugMicController
  → capture
  → sttPipelineService.process(segment, correlationId)
  → response
```

This keeps the existing debug endpoints working while preparing reuse by the continuous listener.

---

### Step 2 — Keep existing debug endpoints functional

The existing endpoints must continue to work:

```http
POST /debug/mic/capture
POST /debug/mic/capture-and-callback
```

Expected behavior remains unchanged:

- `/debug/mic/capture` runs the STT pipeline without orchestrator callback
- `/debug/mic/capture-and-callback` runs the STT pipeline and sends the final STT callback

This step is a safety net before adding continuous runtime behavior.

---

### Step 3 — Add open-line segment recording

Create a recorder dedicated to continuous mode.

The current punctual debug capture can keep using `MicrophoneCaptureService`.

The continuous recorder should:

```text
open microphone once
  → record segment 1
  → process segment 1
  → record segment 2
  → process segment 2
  → ...
```

Important distinction:

| Mode | Behavior |
|---|---|
| Debug capture | Opens microphone, captures once, closes microphone |
| Continuous listener | Keeps microphone open and produces successive bounded segments |

In continuous mode, `no-speech-timeout-ms` should not end the listener. The service should keep waiting indefinitely until speech is detected or the listener is stopped.

---

### Step 4 — Add `ContinuousListeningService`

Create the runtime loop.

Target loop:

```text
start()
  → if already running, no-op
  → open microphone
  → state = WAITING_FOR_SPEECH
  → while running:
      segment = recordNextSegment(openLine)
      state = PROCESSING
      result = sttPipelineService.process(segment, correlationId)
      lastResult = result
      state = WAITING_FOR_SPEECH
  → close microphone
  → state = STOPPED
```

For V1, this loop should be single-threaded:

```text
capture segment
  → process segment
  → return to listening
```

This means the listener will not capture another user utterance while Whisper is processing.
That is acceptable for the first version because it is much simpler, deterministic and easier to debug.

---

### Step 5 — Add listener control endpoints and health reporting

Expose runtime control endpoints:

```http
POST /listener/start
POST /listener/stop
```

Expose runtime status through Actuator health:

```http
GET /actuator/health
```

#### `POST /listener/start`

Starts the continuous listener loop if it is not already running.

Example response:

```json
{
  "running": true,
  "state": "WAITING_FOR_SPEECH"
}
```

#### `POST /listener/stop`

Stops the continuous listener loop and closes the microphone line.

Example response:

```json
{
  "running": false,
  "state": "STOPPED"
}
```

#### `GET /actuator/health`

Returns microphone health and continuous listener runtime details.

Example excerpt:

```json
{
  "components": {
    "microphoneCapture": {
      "status": "UP"
    },
    "continuousListener": {
      "status": "UP",
      "details": {
        "running": true,
        "state": "WAITING_FOR_SPEECH",
        "activeCorrelationId": null,
        "lastSegmentAt": "2026-04-26T18:40:15.123Z",
        "lastDecision": "ACCEPTED",
        "lastTranscript": "Hello Tai",
        "lastError": null
      }
    }
  }
}
```

There is no dedicated `GET /listener/status` endpoint in this plan.

---

### Step 6 — Manual validation scenarios

Validate the continuous listener manually before adding new orchestrator behavior.

Scenarios:

1. Start listener and stay silent.
   - Expected: state remains `WAITING_FOR_SPEECH`.

2. Start listener and say one short valid utterance.
   - Expected: `WAITING_FOR_SPEECH → CAPTURING → PROCESSING → WAITING_FOR_SPEECH`.

3. Say two utterances separated by silence.
   - Expected: two distinct bounded segments and two distinct pipeline results.

4. Say one long utterance.
   - Expected: capture stops at `max-recording-ms` if no silence is detected.

5. Produce noise or clap.
   - Expected: segment is rejected by pre-filter or final gatekeeper, then listener returns to waiting.

6. Stop listener while waiting.
   - Expected: microphone closes and state becomes `STOPPED`.

7. Stop listener during capture or processing.
   - Expected: listener exits cleanly without leaving the microphone line open.

The main observable endpoint for these scenarios is:

```http
GET /actuator/health
```

---

### Step 7 — Add `auto-start` only after manual validation

Once manual control is reliable, add optional startup behavior.

```yaml
tai:
  stt:
    listener:
      auto-start: false
      continue-on-error: true
      delete-audio-after-processing: true
```

Recommended default for development:

```yaml
auto-start: false
```

Recommended later default for V1 runtime:

```yaml
auto-start: true
```

`auto-start` should be enabled only after the listener loop is validated through `/listener/start`, `/listener/stop` and `/actuator/health`.

---

## Future step: speechStarted event

The `speechStarted` callback is intentionally not part of the first continuous listener step.

It will be added after the listener reliably detects and bounds speech segments.

Future flow:

```text
WAITING_FOR_SPEECH
  → speech detected
  → callback /events/stt/speech-started
  → orchestrator can stop TTS immediately
  → listener continues recording until speechEnded
  → final STT callback
```

This future step will require orchestrator changes because it introduces a new inbound STT event and new barge-in behavior.

---

## Exposed debug endpoints

### `POST /debug/mic/capture`

Runs the full STT pipeline but does **not** send any callback to the orchestrator.

Use this endpoint to debug capture, Whisper and gatekeeper decisions locally.

```bash
curl -X POST "http://localhost:8094/debug/mic/capture?correlationId=test-1"
```

Flow:

```text
MicCapture
  → PreFiltering
  → Whisper transcription if accepted by pre-filter
  → PostFiltering
  → JSON result
```

---

### `POST /debug/mic/capture-and-callback`

Runs the full STT pipeline and sends the resulting STT event to the orchestrator.

```bash
curl -X POST "http://localhost:8094/debug/mic/capture-and-callback?correlationId=test-1"
```

Flow:

```text
MicCapture
  → PreFiltering
  → Whisper transcription if accepted by pre-filter
  → PostFiltering
  → Orchestrator callback
  → JSON result
```

Use this endpoint for end-to-end tests with the orchestrator.

---

## Example accepted response

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

> Note: captured WAV files are deleted after processing. The `audioFile` field remains useful for debugging the path that was used during the pipeline, but the file may no longer exist when the response is read.

---

## Example pre-filter rejection

If pre-filter rejects the segment, Whisper is skipped:

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

## Swagger UI

```text
http://localhost:8094/docs
```

OpenAPI JSON:

```text
http://localhost:8094/v3/api-docs
```

---

## Required services

Start `tai-stt-whisper` before testing the full STT pipeline:

```text
http://localhost:8095
```

The listener sends captured WAV bytes to:

```text
POST /whisper/transcribe-raw
```

Transport details:

```text
Content-Type: audio/wav
Body: raw WAV bytes
X-Correlation-Id: <correlationId>
X-Filename: <captured filename>
```

The Java HTTP client is forced to **HTTP/1.1** to avoid Uvicorn HTTP upgrade/body issues.

---

## Orchestrator callbacks

The endpoint `/debug/mic/capture-and-callback` maps the final gatekeeper decision to one of the STT callbacks.

The future continuous listener can reuse the same final callback mapping after each completed segment.

### Accepted transcript

Sent when the final decision is accepted.

```http
POST /events/stt/transcript-accepted
```

Payload:

```json
{
  "eventId": "...",
  "createdAt": "2026-04-26T16:42:10.123Z",
  "source": "STT_SERVICE",
  "correlationId": "test-1",
  "text": "Hello Tai",
  "language": "en",
  "languageProbability": 0.85,
  "durationMs": 3200,
  "averageEnergy": 132.22,
  "reason": "ACCEPTED",
  "suspicionScore": 0
}
```

### Unintelligible transcript

Sent when the final decision rejects the segment as unintelligible.

```http
POST /events/stt/transcript-unintelligible
```

Typical reasons:

- `EMPTY_TRANSCRIPT`
- `UNSUPPORTED_LANGUAGE`
- `SUSPICIOUS_SEGMENT`

### Noise

Sent when the final decision rejects the segment as noise.

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

Used before Whisper to avoid unnecessary transcription.

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

    listener:
      auto-start: false
      continue-on-error: true
      delete-audio-after-processing: true

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
      transcribe-raw-path: /whisper/transcribe-raw

    orchestrator:
      base-url: http://localhost:8080
      connect-timeout-ms: 3000
      read-timeout-ms: 10000
      callbacks:
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
| `tai.stt.capture.no-speech-timeout-ms` | Early stop timeout when no speech is detected in debug capture mode |

### Listener properties

| Property | Description |
|---|---|
| `tai.stt.listener.auto-start` | Starts the continuous listener automatically when the service starts |
| `tai.stt.listener.continue-on-error` | Keeps the listening loop alive after recoverable processing errors |
| `tai.stt.listener.delete-audio-after-processing` | Deletes captured WAV files after STT processing |

### Gatekeeper properties

| Property | Description |
|---|---|
| `tai.stt.gatekeeper.allowed-languages` | Languages accepted by Tai |
| `tai.stt.gatekeeper.reject-audio-duration-ms` | Rejects very short segments |
| `tai.stt.gatekeeper.suspicious-audio-duration-ms` | Adds suspicion for short but not rejected segments |
| `tai.stt.gatekeeper.reject-average-energy-threshold` | Rejects weak audio |
| `tai.stt.gatekeeper.suspicious-language-probability-threshold` | Adds suspicion when language confidence is low |
| `tai.stt.gatekeeper.reject-suspicion-score` | Suspicion score threshold for rejection |
| `tai.stt.gatekeeper.min-voiced-ratio` | Minimum ratio of voiced chunks required before Whisper |

### Whisper properties

| Property | Description |
|---|---|
| `tai.stt.whisper.base-url` | Base URL of the Whisper transcription service |
| `tai.stt.whisper.connect-timeout-ms` | HTTP connection timeout |
| `tai.stt.whisper.read-timeout-ms` | HTTP read timeout for transcription |
| `tai.stt.whisper.transcribe-raw-path` | Raw WAV endpoint used by the listener |

### Orchestrator properties

| Property | Description |
|---|---|
| `tai.stt.orchestrator.base-url` | Base URL of the Tai orchestrator |
| `tai.stt.orchestrator.connect-timeout-ms` | HTTP connection timeout for callbacks |
| `tai.stt.orchestrator.read-timeout-ms` | HTTP read timeout for callbacks |
| `tai.stt.orchestrator.callbacks.transcript-accepted-path` | Callback path for accepted transcript events |
| `tai.stt.orchestrator.callbacks.transcript-unintelligible-path` | Callback path for unintelligible transcript events |
| `tai.stt.orchestrator.callbacks.transcript-noise-path` | Callback path for noise events |

---

## Health

```http
GET /actuator/health
```

Current health checks cover local microphone capture.

| Component | Meaning |
|---|---|
| `microphoneCapture` | Checks whether the configured Java Sound microphone line is supported |

After continuous listener mode is implemented, health should also expose:

| Component | Meaning |
|---|---|
| `continuousListener` | Reports listener loop state and latest runtime details |

Expected `continuousListener` details:

- `running`
- `state`
- `activeCorrelationId`
- `lastSegmentAt`
- `lastDecision`
- `lastTranscript`
- `lastError`

This replaces the idea of a dedicated `GET /listener/status` endpoint.

---

## Local run order

```text
1. Start tai-orchestrator
2. Start tai-stt-whisper
3. Start tai-stt-listener
4. Call /debug/mic/capture-and-callback
```

For local pipeline debugging without orchestrator:

```text
1. Start tai-stt-whisper
2. Start tai-stt-listener
3. Call /debug/mic/capture
```

For future continuous listener mode:

```text
1. Start tai-stt-whisper
2. Start tai-stt-listener
3. POST /listener/start
4. Speak naturally
5. GET /actuator/health
6. POST /listener/stop
```
