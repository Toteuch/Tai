# Tai Orchestrator

## Overview

The **Tai Orchestrator** is the central event-driven service of Tai.

It receives inbound events from the STT, LLM and TTS services, translates them into internal events, updates the session context, and coordinates the next command to send.

```text
inbound service event
  → internal event
  → focused handler
  → session update
  → optional outbound service command
  → completion event
```

The orchestrator owns:

- conversation turn lifecycle
- session context
- event routing
- barge-in decisions
- LLM request orchestration
- TTS request/stop orchestration
- conversation history persistence in memory
- centralized conversation and performance logs

---

## Architecture

```text
Inbound controllers
  ↓
Inbound event handlers
  ↓
Internal event publisher
  ↓
Internal event handlers
  ↓
SessionContext
  ↓
External service clients
```

Main runtime areas:

| Area | Role |
|---|---|
| STT event controllers | Receive STT callbacks |
| LLM event controllers | Receive LLM callbacks |
| TTS event controllers | Receive TTS callbacks |
| Internal event handlers | Apply conversation decisions |
| `SessionContext` | Holds active turn, history, speech/thinking state and metrics |
| `ContextAssembler` | Builds LLM input from system prompt, history and active turn |
| `LlmClient` | Sends generation requests to the LLM service |
| `TtsClient` | Sends speech and stop requests to the TTS service |
| Performance logger | Writes performance metrics |
| Conversation logger | Writes conversation history logs |

---

## Event model

The orchestrator separates inbound facts from internal decisions.

```text
Inbound event
  → received from an external service

Internal event
  → emitted by the orchestrator to model a local decision or transition
```

Examples:

| Inbound event | Internal event | Responsibility |
|---|---|---|
| `SttSpeechStartedEvent` | `UserSpeechStartedEvent` | Handle immediate user barge-in |
| `SttTranscriptAcceptedEvent` | `UserUtteranceAcceptedEvent` | Create a new user turn and call the LLM |
| `SttTranscriptUnintelligibleEvent` | `ClarificationRequestEvent` | Create a temporary clarification turn and call the LLM |
| `LlmResponseCompletedEvent` | `AssistantReplyAcceptedEvent` | Attach assistant reply and request TTS |
| `LlmResponseFailedEvent` | `AssistantReplyFailedEvent` | Complete turn without assistant reply |
| `TtsPlaybackStartedEvent` | `AssistantSpeechStartedEvent` | Mark assistant as speaking |
| `TtsPlaybackCompletedEvent` | `AssistantSpeechCompletedEvent` | Complete assistant speech and finish turn |
| `TtsPlaybackFailedEvent` | `AssistantSpeechFailedEvent` | Complete turn after TTS failure |

---

## Barge-in responsibility

`UserSpeechStartedEvent` is the internal event responsible for barge-in.

It handles:

- interruption of the current assistant flow when needed
- stopping TTS playback through `TtsClient` when needed
- marking the interrupted or superseded turn consistently

`UserUtteranceAcceptedEvent` and `ClarificationRequestEvent` do not handle barge-in. They focus on turn creation and LLM request orchestration.

```text
speech started
  → barge-in / stop current assistant output

accepted transcript
  → create user turn
  → call LLM

unintelligible transcript
  → create clarification turn
  → call LLM
```

---

## Session context

`SessionContext` stores the current conversation runtime state.

Important fields:

| Field | Meaning |
|---|---|
| `activeTurn` | Current in-progress conversation turn |
| `turns` | Completed conversation history |
| thinking state | Whether an LLM generation is in progress |
| speaking state | Whether TTS is preparing/speaking/silent |
| turn metrics map | Metrics keyed by correlation id |

A conversation turn is persisted into history only when `ConversationTurnCompletedEvent` is handled.

---

## Turn metrics

The orchestrator centralizes performance metrics by correlation id.

Metrics are updated by internal events and logged at turn completion.

Typical metric line:

```text
TURN metrics | correlationId=... totalTurnMs=... startedFrom=USER_SPEECH_STARTED transcriptDurationMs=... speechToTranscriptMs=... llmGenerationMs=... ttsSynthesisMs=... assistantFirstAudioLatencyMs=... ttsSpeechDurationMs=...
```

Current fields:

| Field | Meaning |
|---|---|
| `correlationId` | Correlation id of the turn |
| `totalTurnMs` | Duration from speech start or accepted utterance to metrics logging |
| `startedFrom` | Metrics start source: `USER_SPEECH_STARTED` or `USER_UTTERANCE_ACCEPTED` |
| `transcriptDurationMs` | STT transcription duration reported by the STT pipeline |
| `speechToTranscriptMs` | Time between speech-start and accepted transcript |
| `llmGenerationMs` | LLM generation duration |
| `ttsSynthesisMs` | TTS synthesis duration |
| `assistantFirstAudioLatencyMs` | Time between accepted utterance and first TTS audio |
| `ttsSpeechDurationMs` | Assistant speech playback duration |

Metric overwrite attempts are rejected and logged through the dedicated error logger.

---

## Inbound callback endpoints

### STT callbacks

```http
POST /events/stt/speech-started
POST /events/stt/transcript-accepted
POST /events/stt/transcript-unintelligible
POST /events/stt/transcript-noise
```

### LLM callbacks

```http
POST /events/llm/response-completed
POST /events/llm/response-failed
```

### TTS callbacks

```http
POST /events/tts/playback-started
POST /events/tts/playback-completed
POST /events/tts/playback-failed
```

The callback payloads carry a `correlationId` used to match inbound events with the current active turn and metrics entry.

---

## Voice input flow

```text
SttSpeechStartedEvent
  → UserSpeechStartedEvent
  → optional barge-in

SttTranscriptAcceptedEvent
  → UserUtteranceAcceptedEvent
  → active turn created
  → context assembled
  → LLM request sent

LlmResponseCompletedEvent
  → AssistantReplyAcceptedEvent
  → assistant reply attached
  → TTS request sent if enabled

TtsPlaybackStartedEvent
  → AssistantSpeechStartedEvent

TtsPlaybackCompletedEvent
  → AssistantSpeechCompletedEvent
  → ConversationTurnCompletedEvent
  → turn persisted
  → metrics logged
```

---

## Manual text input flow

```text
UiManualTextInputReceivedEvent
  → UserUtteranceAcceptedEvent
  → active turn created
  → context assembled
  → LLM request sent
  → normal assistant response flow
```

Manual text input does not involve `SttSpeechStartedEvent`.

---

## LLM failure flow

```text
UserUtteranceAcceptedEvent
  → LLM request sent

LlmResponseFailedEvent
  → AssistantReplyFailedEvent
  → ConversationTurnCompletedEvent
  → turn finalized without assistant reply
```

No TTS request is sent when the LLM fails.

---

## TTS failure flow

```text
AssistantReplyAcceptedEvent
  → assistant reply attached
  → TTS request sent

TtsPlaybackFailedEvent
  → AssistantSpeechFailedEvent
  → ConversationTurnCompletedEvent
  → turn finalized with assistant text
```

The assistant text remains attached to the turn even if synthesis or playback fails.

---

## Barge-in while LLM is generating

```text
first user utterance
  → LLM generation starts

second SttSpeechStartedEvent
  → UserSpeechStartedEvent
  → current active turn superseded before assistant reply

second SttTranscriptAcceptedEvent
  → UserUtteranceAcceptedEvent
  → new active turn created

late first LlmResponseCompletedEvent
  → ignored as stale
```

Stale LLM responses are rejected by correlation id.

---

## Barge-in while TTS is preparing or speaking

```text
assistant reply accepted
  → TTS preparing/speaking

SttSpeechStartedEvent
  → UserSpeechStartedEvent
  → active assistant output interrupted
  → TtsClient.stop(...) called when needed
  → interrupted turn finalized

SttTranscriptAcceptedEvent
  → UserUtteranceAcceptedEvent
  → next user turn created
```

Interrupted turns are stored with interruption metadata.

---

## Unintelligible STT flow

```text
SttSpeechStartedEvent
  → UserSpeechStartedEvent
  → optional barge-in

SttTranscriptUnintelligibleEvent
  → ClarificationRequestEvent
  → temporary clarification turn created
  → LLM request sent
  → assistant clarification response
  → turn completed without history persistence
```

Clarification turns are short-lived and are not kept in conversation history.

---

## STT noise flow

```text
SttSpeechStartedEvent
  → UserSpeechStartedEvent
  → optional barge-in

SttTranscriptNoiseEvent
  → ignored for conversation turn creation
```

Noise does not create a conversation turn.

---

## State transitions summary

| Phase | Event | State change |
|---|---|---|
| User speech starts | `UserSpeechStartedEvent` | interrupt active assistant flow if needed |
| Accepted input | `UserUtteranceAcceptedEvent` | thinking → generating |
| Clarification needed | `ClarificationRequestEvent` | thinking → generating |
| LLM completed | `LlmResponseCompletedEvent` | thinking → idle |
| TTS requested | `AssistantReplyAcceptedEvent` | speaking → preparing |
| Speaking start | `AssistantSpeechStartedEvent` | speaking → speaking |
| Speaking end | `AssistantSpeechCompletedEvent` | speaking → silent |
| Turn completion | `ConversationTurnCompletedEvent` | active turn → history |

---

## Logging

The orchestrator writes dedicated logs for different concerns.

| Logger | Purpose | Location |
|---|---|
| conversation logger | Conversation turns and assistant/user content | File: *-conversation.log |
| performance logger | Turn-level performance metrics | File: *-performance.log |
| error logger | Metric overwrite issues and dedicated error events | Console |
| context logger | Changes in the context | Console |
| decision logger | Decisions made (eg: ignoring stall events) | Console |
| trace logger | Technical low level tracing | Console |

The log directory is configured through application properties.

---

## Key design principles

- Events represent facts, not intentions.
- One event has one responsibility.
- Inbound events reflect external service callbacks.
- Internal events model orchestrator decisions.
- Conversation state changes happen inside internal event handlers.
- Barge-in is owned by `UserSpeechStartedEvent`.
- Conversation turns are persisted only on `ConversationTurnCompletedEvent` and `UserSpeechStartedEvent`
