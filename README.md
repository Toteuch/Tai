# 🤖 Tai — Local Multimodal AI Assistant

## 📌 Overview

**Tai** is a fully local, real-time multimodal AI assistant built as a modular system of specialized services.

The project aims to provide a voice-first assistant capable of:

- understanding spoken input
- reasoning with a local LLM
- answering through local speech synthesis
- supporting barge-in when the user starts speaking
- exposing observable runtime state and performance metrics
- evolving toward a real-time UI, 3D avatar and screen vision capabilities

Tai is designed around a simple idea:

> Keep intelligence local, split responsibilities cleanly, and make every component replaceable.

---

## 🧠 Core Vision

Tai is not a monolithic AI application.

It is a coordinated system of specialized services:

```text
voice input
  → STT pipeline
  → Orchestrator
  → LLM service
  → TTS service
  → audio output
  → UI / avatar feedback
```

The architecture favors:

- local execution
- service isolation
- event-driven communication
- clear ownership of state and decisions
- measurable performance at each step of the pipeline

---

## 🏗️ Architectural Principles

### 🔁 Event-driven orchestration

Tai is built around events and callbacks.

Services emit facts:

```text
speech started
transcript accepted
LLM response completed
TTS playback started
TTS playback completed
```

The orchestrator receives those facts, updates the session state, and decides what should happen next.

### 🧩 Specialized services

Each service has one primary responsibility:

```text
STT listener       → capture and segment speech
STT transcription → transcribe audio
Orchestrator      → own conversation decisions
LLM service       → generate assistant text
TTS service       → synthesize and play speech
UI                → display state and controls
Avatar            → render visual presence
Vision            → analyze the screen on demand
```

### 🔄 Replaceable components

The system is designed so individual capabilities can evolve independently:

- Whisper can be replaced by another STT engine.
- Piper can be replaced by another TTS engine.
- Ollama models can be swapped.
- UI and avatar layers can evolve without changing the conversation core.

---

## 🗺️ High-Level Architecture

```text
┌────────────────────┐
│ Microphone / UI    │
└─────────┬──────────┘
          │
          ▼
┌────────────────────┐
│ STT Pipeline       │
│ capture + whisper  │
└─────────┬──────────┘
          │ events
          ▼
┌────────────────────┐
│ Orchestrator       │
│ state + decisions  │
└─────────┬──────────┘
          │ service call
          ▼
┌────────────────────┐
│ LLM                │
│ service            │
└─────────┬──────────┘
          │ events
          ▼
┌────────────────────┐
│ Orchestrator       │
│ state + decisions  │
└─────┬────────────┬─┘
      │            │
      ▼            ▼
┌────────────┐  ┌──────────┐
│ UI/Avatar  │  │   TTS    │
│ layer      │  │ service  │
└────┬───────┘  └────┬─────┘
     │               │
     ▼               ▼
 UI / Avatar    audio output
 layer
```

---

## 🧱 Main System Components

### 🧭 Orchestrator

The orchestrator is the central non-AI decision layer.

It owns:

- event routing
- session state
- active conversation turn
- conversation history
- barge-in decisions
- LLM request orchestration
- TTS request and stop orchestration
- conversation logs
- performance metrics aggregation

The orchestrator does not perform transcription, generation or speech synthesis itself. It coordinates specialized services.

---

### 🎤 STT Pipeline

The STT pipeline converts microphone input into accepted or rejected text events.

The system is split into two responsibilities:

- continuous microphone listening and speech segmentation
- pure Whisper transcription

This separation keeps audio capture, gatekeeping and transcription independent.

Key concepts:

- continuous microphone listening
- speech-start detection
- speech-end detection
- pre-filtering before transcription
- Whisper transcription
- post-filtering after transcription
- STT events sent to the orchestrator

---

### 🧠 LLM Service

The LLM service is responsible for text generation.

Tai targets local models through Ollama.

The model is treated as a replaceable reasoning backend. The rest of the system interacts with it through a service boundary rather than embedding model-specific logic in the orchestrator.

Target model family:

- 7B–8B local models for latency and quality balance
- larger or specialized models when hardware allows

---

### 🔊 TTS Service

The TTS service converts assistant replies into spoken audio.

The current direction is local speech synthesis through Piper.

Key concepts:

- local voice model
- local WAV synthesis
- local audio playback
- playback lifecycle callbacks
- stop command support for barge-in

---

### 🖥️ UI

The UI is the real-time control and monitoring surface for Tai.

Capabilities:

- display global system state
- display component health
- show current conversation turn
- show latest user transcript
- show latest assistant reply
- expose conversation history
- expose runtime toggles and diagnostics

---

### 👤 3D Avatar

The avatar layer is a visual presence for Tai.

Capabilities:

- VRM-based rendering
- idle animation
- speech animation
- lip sync
- facial expressions
- emotion and state visualization

The avatar remains a rendering layer, not a business logic layer.

---

### 👁️ Screen Vision

Screen vision is an on-demand capability.

Pipeline:

```text
screen capture
  → OCR / visual extraction
  → structured text summary
  → LLM context
```

Screen analysis is intentionally modeled as an explicit capability, not as a permanent background observer.

---

## 🔄 Simplified Voice Flow

```text
1. User starts speaking
2. STT detects speech start
3. Orchestrator handles possible barge-in
4. STT captures and transcribes the utterance
5. Orchestrator creates a conversation turn
6. LLM generates an assistant response
7. TTS synthesizes and plays speech
8. Orchestrator finalizes the conversation turn
9. Metrics and conversation logs are written
```

---

## 🧭 Assistant State Model

Tai tracks several runtime states to keep the system observable and controllable:

- listening state
- thinking state
- speaking state
- interruption state
- active conversation turn
- session context
- health status
- performance metrics
- UI state
- avatar state
- emotion state

---

## 📊 Performance and Observability

Tai is designed to make latency visible.

The orchestrator centralizes turn-level performance metrics, including:

- STT transcription duration
- speech-to-transcript latency
- LLM generation duration
- TTS synthesis duration
- first-audio latency
- TTS speech duration
- total turn duration

This makes it possible to identify whether a delay comes from:

```text
speech capture
STT
LLM
TTS synthesis
audio playback
or orchestration
```

Health and runtime state are exposed through service-level health endpoints and consolidated by the orchestration layer.

---

## 🧰 Tech Stack

| Area | Technology |
|---|---|
| Language runtime | Java 21, Python |
| Java framework | Spring Boot |
| Python API framework | FastAPI |
| Build tool | Maven |
| STT transcription | faster-whisper / Whisper |
| STT acceleration | CUDA-capable GPU |
| LLM runtime | Ollama |
| TTS engine | Piper |
| API documentation | OpenAPI / Swagger UI |
| Health checks | Spring Actuator / service health endpoints |
| Logging | Logback and dedicated domain loggers |
| UI target | Web frontend |
| Avatar target | VRM + Three.js |

---

## 🚀 Development Roadmap

### 🎙️ V1 — Voice Assistant Core

- Voice input ✅
- STT pipeline ✅
- Local LLM response generation ✅
- Local TTS playback ✅
- Event-driven orchestrator ✅
- Session management ✅
- Barge-in support ✅
- Conversation logs ✅
- Performance metrics ✅
- Basic monitoring surface

---

### 🖥️ V2 — User Interface

- System health dashboard
- Conversation history
- Current turn visualization
- Thinking / speaking / listening states
- Runtime controls
- Debug panels

---

### 👤 V3 — 3D Avatar

- VRM avatar rendering
- Lip sync
- Basic facial expressions
- State-driven animations
- Avatar integration with assistant state

---

### 👁️ V4 — Screen Vision

- On-demand screen capture
- OCR / visual extraction
- Structured screen analysis
- LLM integration

---

### 🧬 V5 — Specialization

- Long-term memory
- Retrieval-augmented generation
- Domain-specific behavior
- Lightweight model adaptation
- Curated dataset processing

---

## 🧠 LLM Strategy

Tai uses local models only.

The project favors:

- small-to-medium local models
- careful prompt design
- contextual orchestration
- performance-aware model selection
- retrieval and memory over full fine-tuning

The goal is not to depend on one specific model, but to build a system that can swap models as local AI tooling improves.

---

## 🛡️ Safety and Filtering

Tai keeps model behavior configurable at the application level.

Filtering and policies are expected to be handled outside the model through:

- configurable behavior rules
- optional text filters
- runtime toggles
- orchestration policies

This keeps model execution local while allowing the application to decide how strict or permissive the assistant should be.

---

## 🎯 Project Goals

- Fully local operation
- Real-time voice interaction
- Modular service architecture
- Replaceable AI components
- Observable runtime behavior
- Clear separation between AI execution and orchestration decisions
- Developer-friendly experimentation

---

## 🖥️ Hardware Target

Primary hardware target:

- NVIDIA RTX 4070 with 12GB VRAM
- 64GB RAM

The architecture is designed to support local real-time interaction under multi-service load.

---

## 🧭 Philosophy

> Tai prioritizes architecture over model size.

The goal is not to build the biggest AI, but a responsive, modular and controllable assistant that can evolve service by service.
