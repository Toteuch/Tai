# 🎙️ Tai STT Microservice

## Overview

The **STT (Speech-To-Text) microservice** is responsible for:

- Capturing audio from the microphone
- Detecting speech segments (VAD)
- Transcribing audio using Whisper
- Filtering results via a Gatekeeper
- Emitting normalized STT events
- Optionally sending callbacks to the orchestrator

The service is designed to be **fully decoupled** from the orchestrator and can evolve independently.

---

# 🧠 Architecture

```
Microphone Capture
        ↓
Speech Segmentation (VAD)
        ↓
Pre-filter (Gatekeeper)
        ↓
(Conditional) Whisper Transcription
        ↓
Post-filter (Gatekeeper)
        ↓
Event Mapping
        ↓
(Optional) HTTP Callback → Orchestrator
```

---

# 🔌 External Interfaces

## Debug Endpoints

### POST /debug/mic/capture

Capture + process without callback.

### POST /debug/mic/capture-and-callback

Capture + process + send callback to orchestrator.

---

## Callback to Orchestrator

```
{callback_base_url}/{callback_name}
```

### Event types

- STT_TRANSCRIPT_ACCEPTED → transcript-accepted
- STT_TRANSCRIPT_NOISE → transcript-noise
- STT_TRANSCRIPT_UNINTELLIGIBLE → transcript-unintelligible

---

# 🧩 Internal Components

## MicrophoneCaptureService

Handles:

- Audio capture
- Voice activity detection (VAD)
- Segment metrics (energy, duration, voiced ratio)

---

## WhisperService

- Loads model once at startup
- Performs transcription
- Detects language

---

## TranscriptGatekeeper

- Pre-filter (before Whisper)
- Post-filter (after Whisper)
- Decision: ACCEPTED / NOISE / UNINTELLIGIBLE

---

## STT Pipeline

```
capture → pre-filter → whisper (optional) → decision → event → callback
```

---

# ⚙️ Configuration

## Whisper

- enabled
- python-executable
- script-path
- model-size
- device
- compute-type

---

## Capture

- output-dir
- sample-rate
- sample-size-bits
- channels
- buffer-size
- silence-threshold
- silence-duration-ms
- min-recording-ms
- max-recording-ms
- no-speech-timeout-ms

---

## Gatekeeper

- reject-audio-duration-ms
- suspicious-audio-duration-ms
- reject-average-energy-threshold
- suspicious-language-probability-threshold
- reject-suspicion-score
- min-voiced-ratio

---

# 🧪 Decision Logic

## Pre-filter

Reject before Whisper if:

- No speech detected
- Audio too short
- Audio too weak
- Not enough voiced signal

---

## Post-filter

### ACCEPTED

- Valid text
- Supported language (fr/en)

### NOISE

- Silence
- Weak audio
- No speech

### UNINTELLIGIBLE

- Unsupported language
- Empty transcript with speech
- Low language confidence

---

# ⚡ Performance Strategy

- Whisper loaded once at startup
- Whisper skipped when:
    - silence detected
    - weak audio
    - short segment
    - low voiced ratio

---

## Timings

```
captureMs
whisperMs
totalMs
whisperSkipped
```

---

# 🔄 Event Model

Each event contains:

- eventId
- createdAt
- source (STT_SERVICE)
- correlationId
- durationMs
- averageEnergy
- language
- languageProbability
- reason
- text (if accepted)

---

# 🚀 Future Improvements

- Continuous microphone listening
- Speech-started event (for TTS interruption)
- Health endpoint
- Streaming STT
- Dedicated Gatekeeper AI
- Service split:
    - capture-service (Java)
    - stt-service (Python)
    - gatekeeper-service

---

# ✅ Summary

The STT microservice is:

- Event-driven
- Fully decoupled
- Optimized for real-time interaction
- Designed for future modularization
