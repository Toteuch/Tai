# 🤖 Tai — Local Multimodal AI Assistant

## 📌 Project Overview

**Tai** is a fully local, real-time multimodal AI assistant built as a modular system of specialized services.

The assistant is designed to:

- Understand voice input (Speech-to-Text)
- Generate intelligent responses (LLM)
- Speak responses (Text-to-Speech)
- Render a 3D avatar (VTuber-like)
- Analyze the screen on demand
- Provide a real-time interactive interface

The system is **event-driven, scalable, and modular**, allowing independent evolution of each component.

---

## 🧠 Core Vision

Tai is not a monolithic AI.

It is a **coordinated system of specialized components**:

- A central **LLM (reasoning engine)**
- Independent **AI services (STT, TTS, Vision, Avatar)**
- A central **Orchestrator**
- A real-time **event-driven communication layer**

---

## 🏗️ Architecture

### 🔁 Design Pattern

The system follows an **event-driven architecture**, with three main categories:

### 1. Event Producers

- Microphone input
- STT service
- User interface interactions
- Screen capture service

### 2. Decision Makers

- Orchestrator (central logic)
- LLM (reasoning and response generation)

### 3. Executors

- TTS service
- Avatar renderer
- UI layer

---

## 🧩 Main Components

### 🧠 LLM Core

- Role: reasoning and text generation
- Runtime: Ollama
- Target models:
    - `llama3.1:8b` (recommended)
    - `qwen3:8b` (fallback)

---

### 🎤 Speech-to-Text (STT)

- Converts voice input into text
- Supports streaming transcription
- Handles noise and partial recognition

---

### 🔊 Text-to-Speech (TTS)

- Converts text into audio
- Optimized for low latency
- Provides timing data for lip sync

---

### 👤 3D Avatar System

- VRM-based avatar rendering
- Supports:
    - Idle animation
    - Lip sync
    - Facial expressions
- Fully event-driven (no business logic)

---

### 🖥️ Screen Vision Module

- Triggered **on demand only**
- Pipeline:
    - Screen capture
    - OCR (text extraction)
    - Structured analysis
- Output is transformed into text before being sent to the LLM

---

### 🎛️ User Interface

- Real-time monitoring and control
- Features:
    - Display STT transcription
    - Display assistant responses (subtitles)
    - Toggle filters (e.g., obscenity filter)
    - Change avatar / voice
    - Debug system state

---

## ⚙️ Orchestrator

The orchestrator is the **central non-AI brain** of the system.

### Responsibilities:

- Event routing
- State management
- Session handling
- Triggering LLM calls
- Managing interruptions
- Applying policies and filters

---

## 🔄 Event Flow (Simplified)

1. User speaks
2. STT produces partial and final text
3. Orchestrator processes input
4. LLM generates response (streamed)
5. TTS converts response to audio
6. Avatar reacts (speech + emotion)
7. UI updates subtitles and state

---

## 🧠 Assistant State Model

- listening_state
- speaking_state
- thinking_state
- interruption_state
- mood_state
- emotion_state
- avatar_state
- active_capabilities
- safety_flags
- ui_flags
- session_context

---

## 🚀 Development Roadmap

### V1 — Voice Assistant (Core)

- STT + LLM + TTS
- Real-time subtitles
- Basic UI controls
- Event system
- Session management

---

### V2 — 3D Avatar

- VRM avatar rendering
- Lip sync
- Basic emotions
- Animation triggers

---

### V3 — Screen Vision

- On-demand screen capture
- OCR + structured analysis
- LLM integration

---

### V4 — Specialization

- Long-term memory
- Fine-tuning (LoRA / RAG)
- Domain-specific behaviors
- Reddit dataset processing (curated)

---

## 🧪 LLM Strategy

- Use **local models only**
- Prefer **7B–8B models** for performance balance
- Avoid heavy full fine-tuning
- Focus on:
    - Prompt engineering
    - RAG (Retrieval-Augmented Generation)
    - Lightweight adapters (LoRA)

---

## 🔐 Safety & Filtering

The LLM is intentionally **minimally restricted**.

Content filtering is handled at the application level:

- Optional obscenity filter
- Configurable behavior policies
- Runtime toggles via UI

---

## 🧰 Tech Stack (Planned)

| Component    | Technology              |
|--------------|-------------------------|
| LLM Runtime  | Ollama                  |
| Orchestrator | Java / Kotlin           |
| AI Services  | Python / C++            |
| STT          | Whisper (or equivalent) |
| TTS          | Piper / Kokoro          |
| Avatar       | VRM + Three.js          |
| UI           | Web-based frontend      |

---

## 📦 Project Goals

- Fully local (no cloud dependency)
- Real-time interaction
- Modular and extensible architecture
- Replaceable components
- Developer-friendly system

---

## ⚠️ Constraints

- Hardware target:
    - RTX 4070 (12GB VRAM)
    - 64GB RAM
- Must support real-time performance
- Must remain stable under multi-service load

---

## 🧭 Philosophy

> Tai prioritizes **architecture over model size**.

The goal is not to build the biggest AI, but the most **responsive, modular, and controllable assistant**.
