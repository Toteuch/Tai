# Tai Orchestrator V1 — Event Flow Documentation

## Overview

This document describes the event flows handled by the Tai Orchestrator V1.

The orchestrator follows an event-driven lifecycle:
1. Input event received
2. Internal processing events triggered
3. External services called
4. Completion events finalize the flow

---

## Scenario 1 — Voice Input → Full Assistant Response

### Flow

1. `SttFinalTranscriptReceivedEvent`
2. `UserUtteranceAcceptedEvent`
3. `LlmGenerationRequestedEvent`
4. `LlmResponseCompletedEvent`
5. `AssistantReplyAcceptedEvent`
6. `TtsPlaybackRequestedEvent` (if enabled)
7. `TtsPlaybackStartedEvent`
8. `TtsPlaybackCompletedEvent`
9. `ConversationTurnCompletedEvent`

---

## Scenario 2 — Manual Text Input (No STT)

### Flow

1. `UiManualTextInputReceivedEvent`
2. `UserUtteranceAcceptedEvent`
3. `LlmGenerationRequestedEvent`
4. `LlmResponseCompletedEvent`
5. `AssistantReplyAcceptedEvent`
6. `TtsPlaybackRequestedEvent` (optional)
7. `ConversationTurnCompletedEvent`

---

## Scenario 3 — LLM Failure

### Flow

1. `SttFinalTranscriptReceivedEvent`
2. `UserUtteranceAcceptedEvent`
3. `LlmGenerationRequestedEvent`
4. `LlmResponseFailedEvent`

---

## Scenario 4 — TTS Failure

### Flow

1. `LlmResponseCompletedEvent`
2. `AssistantReplyAcceptedEvent`
3. `TtsPlaybackRequestedEvent`
4. `TtsPlaybackFailedEvent`
5. `ConversationTurnCompletedEvent`

---

## Scenario 5 — Interruption (Barge-in)

### Flow

1. `TtsPlaybackStartedEvent`
2. New `SttFinalTranscriptReceivedEvent`
3. `CurrentSpeechInterruptedEvent`
4. `UserUtteranceAcceptedEvent`
5. New conversation cycle starts

---

## Scenario 6 — TTS Disabled

### Flow

1. `UiTtsToggleChangedEvent (enabled=false)`
2. Normal conversation flow
3. Skip `TtsPlaybackRequestedEvent`
4. Directly emit `ConversationTurnCompletedEvent`

---

## Scenario 7 — Obscenity Filter Toggle

### Flow

1. `UiObscenityFilterToggleChangedEvent`
2. Internal state updated
3. Next LLM responses processed with updated policy

---

## State Transitions Summary

| Phase | Event | State Change |
|------|------|-------------|
| Input | STT Final | listening → processing |
| Thinking | LLM Request | thinking → generating |
| Thinking Done | LLM Response | thinking → idle |
| Speaking Start | TTS Start | speaking → speaking |
| Speaking End | TTS Done | speaking → silent |
| Completion | Turn Completed | reset to idle |

---

## Key Design Principles

- Events represent facts, not intentions
- One event = one responsibility
- Internal events structure the flow
- External events reflect real-world changes
- Orchestrator owns all decisions
