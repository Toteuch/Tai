# 🎤 Speech Segment Acceptance Criteria (V1)

This document defines how a captured audio segment is evaluated before being sent to the conversational pipeline.

The goal is to:

* accept real user speech (even short phrases)
* reject noise / false positives
* remain language-agnostic
* avoid hardcoded dictionaries or word lists

---

## 🧠 Philosophy

A segment is accepted if it **looks like intentional human speech**, not based on *what* is said, but on *how plausible the signal is*.

We prioritize:

1. Audio signal quality
2. Segment duration
3. Transcription structure
4. Confidence signals (secondary)

---

## ✅ Acceptance Decision Model

| Category                | Type        | Criteria                        | Action   | Notes                              |
| ----------------------- | ----------- | ------------------------------- | -------- | ---------------------------------- |
| Empty transcription     | Hard reject | `text == null OR blank`         | ❌ Reject | No usable content                  |
| No alphanumeric content | Hard reject | Only symbols / punctuation      | ❌ Reject | Likely noise                       |
| Very short audio        | Hard reject | `< 250 ms`                      | ❌ Reject | Too short to be meaningful speech  |
| Low energy signal       | Hard reject | Energy below threshold          | ❌ Reject | Likely silence or background noise |
| Corrupted transcription | Hard reject | Unreadable / invalid chars only | ❌ Reject | STT failure                        |

---

## ⚠️ Suspicion Signals (Scoring)

Each signal adds **+1 suspicion point**.

| Signal                  | Condition                            | Rationale                 |
| ----------------------- | ------------------------------------ | ------------------------- |
| Short audio             | `< 500 ms`                           | Could be accidental noise |
| Low language confidence | `< 0.45`                             | STT unsure of meaning     |
| Very short text         | length ≤ 2 AND single token          | Weak signal               |
| Symbol-heavy text       | low letter/digit ratio               | Likely noise artifact     |
| Unusual token           | single strange token (unicode, etc.) | STT ambiguity             |

---

## 🎯 Final Decision Rule

| Score  | Decision |
| ------ | -------- |
| `< 2`  | ✅ Accept |
| `>= 2` | ❌ Reject |

---

## 🧩 Important Notes

* Short phrases like "ok", "yes", "no" are **valid and must not be rejected**
* No whitelist or dictionary is used
* Audio characteristics are more important than text length
* Language probability is **secondary only**

---

# 🧱 Interfaces for Continuous Microphone Pipeline

These interfaces define a modular architecture for continuous voice interaction.

---

## 🎙️ Microphone Stream

Responsibility:

* Opens microphone continuously
* Streams audio buffers
* No knowledge of STT or conversation

---

## 🔊 Voice Activity Detector (VAD)

Responsibility:

* Detect speech vs silence
* Define segment boundaries

---

## 🧱 Speech Segment Buffer

Responsibility:

* Accumulate audio chunks
* Produce a full segment

---

## 🧠 STT Client

Responsibility:

* Transcribe audio to text
* Provide language confidence

---

## 🚦 Transcript Gatekeeper

Responsibility:

* Apply acceptance criteria
* Filter noise before orchestrator

---

## 🧾 Speech Segment Model

```java
public record SpeechSegment(
        long durationMs,
        double averageEnergy,
        Path audioFile
) {}
```

---

## 🔄 Pipeline Flow

```text
MicrophoneStreamService
    → VoiceActivityDetector
        → SpeechSegmentBuffer
            → SttClient
                → TranscriptGatekeeper
                    → UserInputProcessor
```

---

## 🎯 Design Goals

* Fully modular
* Replaceable components (VAD, STT, TTS)
* No business logic leakage into orchestrator
* Clear separation between audio, transcription, and conversation
