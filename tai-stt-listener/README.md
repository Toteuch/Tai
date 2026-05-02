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
- continuous microphone listening
- speech-start detection
- speech-end detection
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
SttPipelineService
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

| Component                           | Role                                                                  |
|-------------------------------------|-----------------------------------------------------------------------|
| `DebugMicController`                | Exposes debug endpoints for capture and capture+callback              |
| `ListenerController`                | Exposes continuous listener start/stop endpoints                      |
| `ContinuousListeningService`        | Owns continuous microphone listening lifecycle                        |
| `SpeechSegmentRecorder`             | Records bounded speech segments from an open microphone line          |
| `MicrophoneCaptureService`          | Captures one microphone segment using Java Sound for debug mode       |
| `SttPipelineService`                | Runs pre-filtering, Whisper transcription, post-filtering and cleanup |
| `SpeechSegment`                     | Contains WAV path and audio metrics                                   |
| `TranscriptGatekeeper`              | Applies pre-filtering and post-filtering decisions                    |
| `HttpWhisperTranscriptionClient`    | Sends raw WAV bytes to `tai-stt-whisper`                              |
| `OrchestratorSttEventClient`        | Sends STT callbacks to the orchestrator                               |
| `CaptureHealthIndicator`            | Checks Java Sound microphone compatibility                            |
| `ContinuousListenerHealthIndicator` | Reports continuous listener runtime state through Actuator            |

---

## Continuous listener mode

The listener can keep the microphone open continuously and process successive user utterances.

Runtime flow:

```text
tai-stt-listener starts
  → opens microphone line
  → waits for speech
  → detects speech start
  → publishes speechStarted callback if enabled
  → records the current utterance
  → detects speech end after configured silence
  → writes WAV segment
  → runs existing STT pipeline:
      PreFiltering
      Whisper transcription
      PostFiltering
  → optionally publishes final STT callback
  → stores/logs latest result
  → returns to WAITING_FOR_SPEECH
```

The continuous listener is controlled by:

```http
POST /listener/start
POST /listener/stop
```

Runtime status is exposed through Actuator health:

```http
GET /actuator/health
```

There is intentionally no `GET /listener/status` endpoint. Runtime state belongs in health because it prepares
orchestrator-level health consolidation.

---

## SpeechStarted callback

The listener can now publish a callback as soon as speech is detected.

This event is emitted **before** the utterance is fully recorded and before Whisper transcription starts.

Its purpose is to let the orchestrator react immediately to user speech, especially for future barge-in behavior.

### Flow

```text
WAITING_FOR_SPEECH
  → energy crosses speech threshold
  → listener creates correlationId for this utterance
  → listener sends /events/stt/speech-started
  → listener continues recording the segment
  → listener detects speech end
  → listener runs STT pipeline
  → listener sends final transcript callback if enabled
```

### Callback endpoint

```http
POST /events/stt/speech-started
```

### Payload

```json
{
    "eventId": "...",
    "createdAt": "2026-04-26T18:40:15.123Z",
    "source": "STT_SERVICE",
    "correlationId": "test-1",
    "averageEnergy": 132.22,
    "peakEnergy": 643.66
}
```

The `correlationId` generated for `speechStarted` must be reused for the final transcript callback of the same
utterance.

### Important behavior

The `speechStarted` callback is emitted only in continuous listener mode.

It is not emitted by:

```http
POST /debug/mic/capture
POST /debug/mic/capture-and-callback
```

Debug endpoints remain request/response oriented and are intended for manual tests.

---

## Orchestrator impact

This step introduces a new callback event.

| Feature                                | Orchestrator impact                    |
|----------------------------------------|----------------------------------------|
| Continuous microphone open             | None                                   |
| Speech segmentation inside listener    | None                                   |
| Final STT callbacks                    | Already supported                      |
| `speechStarted` callback               | Requires orchestrator endpoint/handler |
| Barge-in behavior from `speechStarted` | Requires orchestrator logic            |

The listener can expose the speech-start callback independently, but full value comes when the orchestrator handles it.

Expected orchestrator endpoint:

```http
POST /events/stt/speech-started
```

Expected future orchestrator behavior:

```text
speechStarted received
  → if assistant is speaking/preparing/generating
  → interrupt current assistant output
  → keep listening until final STT transcript arrives
```

---

## Listener states

The continuous listener tracks runtime state:

```java
STOPPED
    STARTING
WAITING_FOR_SPEECH
    CAPTURING
PROCESSING
    ERROR
```

Expected transitions:

```text
STOPPED
  → STARTING
  → WAITING_FOR_SPEECH
  → CAPTURING
  → PROCESSING
  → WAITING_FOR_SPEECH
```

On stop:

```text
WAITING_FOR_SPEECH/CAPTURING/PROCESSING
  → STOPPED
```

On unrecoverable error:

```text
any state
  → ERROR
```

---

## Continuous listener health

The continuous listener runtime state is exposed through Actuator.

```http
GET /actuator/health
```

Example:

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
                "lastError": null,
                "autoStart": false,
                "publishSpeechStartedCallbacks": true,
                "publishFinalCallbacks": true
            }
        }
    }
}
```

Recommended health mapping:

| Listener state                    | Health status              |
|-----------------------------------|----------------------------|
| `WAITING_FOR_SPEECH`              | `UP`                       |
| `CAPTURING`                       | `UP`                       |
| `PROCESSING`                      | `UP`                       |
| `STOPPED` with `auto-start=false` | `UP`                       |
| `STOPPED` with `auto-start=true`  | `OUT_OF_SERVICE` or `DOWN` |
| `ERROR`                           | `DOWN` or `DEGRADED`       |

`STOPPED` should not automatically mean `DOWN` when manual start is expected.

---

## Exposed endpoints

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
    "lastSegmentAt": "2026-04-26T18:40:15.123Z",
    "lastResult": {
        "accepted": true,
        "reason": "ACCEPTED",
        "rejectionCategory": "NONE",
        "text": "Hello Tai",
        "language": "en",
        "completedAt": "2026-04-26T18:40:15.123Z"
    },
    "lastError": null
}
```

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

Runs the full STT pipeline and sends the resulting final STT event to the orchestrator.

```bash
curl -X POST "http://localhost:8094/debug/mic/capture-and-callback?correlationId=test-1"
```

Flow:

```text
MicCapture
  → PreFiltering
  → Whisper transcription if accepted by pre-filter
  → PostFiltering
  → Orchestrator final callback
  → JSON result
```

Use this endpoint for end-to-end tests with the orchestrator.

This endpoint does **not** emit `speechStarted`.

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

> Note: captured WAV files are deleted after processing. The `audioFile` field remains useful for debugging the path
> that was used during the pipeline, but the file may no longer exist when the response is read.

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

### Speech started

Sent when continuous listener detects speech start.

```http
POST /events/stt/speech-started
```

Typical usage:

- notify orchestrator immediately that the user started speaking
- prepare future barge-in handling
- correlate the final transcript callback with the early speech-start event

Payload:

```json
{
    "eventId": "...",
    "createdAt": "2026-04-26T18:40:15.123Z",
    "source": "STT_SERVICE",
    "correlationId": "test-1",
    "averageEnergy": 132.22,
    "peakEnergy": 643.66
}
```

---

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

---

### Unintelligible transcript

Sent when the final decision rejects the segment as unintelligible.

```http
POST /events/stt/transcript-unintelligible
```

Typical reasons:

- `EMPTY_TRANSCRIPT`
- `UNSUPPORTED_LANGUAGE`
- `SUSPICIOUS_SEGMENT`

---

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

| Reason                    | Category | Meaning                       |
|---------------------------|----------|-------------------------------|
| `SEGMENT_MISSING`         | `NOISE`  | No segment was provided       |
| `NO_SPEECH_DETECTED`      | `NOISE`  | Capture did not detect speech |
| `AUDIO_TOO_SHORT`         | `NOISE`  | Segment is too short          |
| `AUDIO_TOO_WEAK`          | `NOISE`  | Audio peak/energy is too weak |
| `NOT_ENOUGH_VOICED_AUDIO` | `NOISE`  | Voiced ratio is too low       |

### Final decisions

Used after Whisper transcription.

| Reason                    | Category                    | Meaning                                            |
|---------------------------|-----------------------------|----------------------------------------------------|
| `ACCEPTED`                | `NONE`                      | Transcript is valid                                |
| `STT_FAILED`              | `NOISE`                     | Whisper call or transcription failed               |
| `EMPTY_TRANSCRIPT`        | `NOISE` or `UNINTELLIGIBLE` | Empty transcription, depending on audio context    |
| `NO_ALPHANUMERIC_CONTENT` | `NOISE`                     | Transcript contains no useful alphanumeric content |
| `UNSUPPORTED_LANGUAGE`    | `UNINTELLIGIBLE`            | Language is not in the allowed language list       |
| `SUSPICIOUS_SEGMENT`      | `UNINTELLIGIBLE`            | Suspicion score reached rejection threshold        |

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
            publish-speech-started-callbacks: true
            publish-final-callbacks: true

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
                speech-started-path: /events/stt/speech-started
                transcript-accepted-path: /events/stt/transcript-accepted
                transcript-unintelligible-path: /events/stt/transcript-unintelligible
                transcript-noise-path: /events/stt/transcript-noise
```

### Capture properties

| Property                               | Description                                                         |
|----------------------------------------|---------------------------------------------------------------------|
| `tai.stt.capture.output-dir`           | Directory where captured WAV files are temporarily written          |
| `tai.stt.capture.sample-rate`          | Audio sample rate                                                   |
| `tai.stt.capture.sample-size-bits`     | Sample size in bits                                                 |
| `tai.stt.capture.channels`             | Number of audio channels                                            |
| `tai.stt.capture.signed`               | Whether PCM samples are signed                                      |
| `tai.stt.capture.big-endian`           | Whether samples are big-endian                                      |
| `tai.stt.capture.buffer-size`          | Audio buffer size used by Java Sound                                |
| `tai.stt.capture.silence-threshold`    | Energy threshold used to detect speech                              |
| `tai.stt.capture.silence-duration-ms`  | Silence duration required to stop after speech                      |
| `tai.stt.capture.min-recording-ms`     | Minimum recording time before silence can stop the segment          |
| `tai.stt.capture.max-recording-ms`     | Hard maximum recording duration                                     |
| `tai.stt.capture.no-speech-timeout-ms` | Early stop timeout when no speech is detected in debug capture mode |

### Listener properties

| Property                                            | Description                                                            |
|-----------------------------------------------------|------------------------------------------------------------------------|
| `tai.stt.listener.auto-start`                       | Starts the continuous listener automatically when the service starts   |
| `tai.stt.listener.continue-on-error`                | Keeps the listening loop alive after recoverable processing errors     |
| `tai.stt.listener.delete-audio-after-processing`    | Deletes captured WAV files after STT processing                        |
| `tai.stt.listener.publish-speech-started-callbacks` | Sends a speech-started callback as soon as speech is detected          |
| `tai.stt.listener.publish-final-callbacks`          | Sends final transcript/noise/unintelligible callbacks after processing |

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

### Whisper properties

| Property                              | Description                                   |
|---------------------------------------|-----------------------------------------------|
| `tai.stt.whisper.base-url`            | Base URL of the Whisper transcription service |
| `tai.stt.whisper.connect-timeout-ms`  | HTTP connection timeout                       |
| `tai.stt.whisper.read-timeout-ms`     | HTTP read timeout for transcription           |
| `tai.stt.whisper.transcribe-raw-path` | Raw WAV endpoint used by the listener         |

### Orchestrator properties

| Property                                                        | Description                                        |
|-----------------------------------------------------------------|----------------------------------------------------|
| `tai.stt.orchestrator.base-url`                                 | Base URL of the Tai orchestrator                   |
| `tai.stt.orchestrator.connect-timeout-ms`                       | HTTP connection timeout for callbacks              |
| `tai.stt.orchestrator.read-timeout-ms`                          | HTTP read timeout for callbacks                    |
| `tai.stt.orchestrator.callbacks.speech-started-path`            | Callback path for speech-started events            |
| `tai.stt.orchestrator.callbacks.transcript-accepted-path`       | Callback path for accepted transcript events       |
| `tai.stt.orchestrator.callbacks.transcript-unintelligible-path` | Callback path for unintelligible transcript events |
| `tai.stt.orchestrator.callbacks.transcript-noise-path`          | Callback path for noise events                     |

---

## Health

```http
GET /actuator/health
```

Health components:

| Component            | Meaning                                                               |
|----------------------|-----------------------------------------------------------------------|
| `microphoneCapture`  | Checks whether the configured Java Sound microphone line is supported |
| `continuousListener` | Reports listener loop state and latest runtime details                |

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

This replaces the idea of a dedicated `GET /listener/status` endpoint.

---

## Local run order

```text
1. Start tai-orchestrator
2. Start tai-stt-whisper
3. Start tai-stt-listener
4. POST /listener/start
5. Speak naturally
6. GET /actuator/health
7. POST /listener/stop
```

For local pipeline debugging without orchestrator:

```text
1. Start tai-stt-whisper
2. Start tai-stt-listener
3. Call /debug/mic/capture
```

For end-to-end debug with final callbacks only:

```text
1. Start tai-orchestrator
2. Start tai-stt-whisper
3. Start tai-stt-listener
4. Call /debug/mic/capture-and-callback
```
