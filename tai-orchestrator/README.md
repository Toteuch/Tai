# Tai Orchestrator

## Overview

The **Tai Orchestrator** is the central event-driven service of Tai.

It receives inbound events from the STT, LLM and TTS services, maps those external facts to internal events, updates the session context, and coordinates the next command to send.

```text
inbound service event
  → internal event mapping
  → focused handler
  → session update
  → optional outbound service command
  → completion event
  → UI runtime projection refresh
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
- debug system health aggregation
- module runtime state used by the UI
- V2 live UI projection and SSE publication

---

## Architecture

```text
Inbound controllers
  ↓
Inbound event handlers
  ↓
TaiEventPublisher
  ↓
Internal event handlers
  ↓
SessionContext + ModuleRuntimeRegistry
  ↓
External service clients / UI projection
```

Main runtime areas:

| Area | Role |
|---|---|
| STT event controllers | Receive STT callbacks |
| LLM event controllers | Receive LLM callbacks |
| TTS event controllers | Receive TTS callbacks |
| Internal event handlers | Apply conversation decisions |
| `TaiEventPublisher` | Publishes orchestrator domain events |
| `SessionContext` | Holds active turn, completed history, speech/thinking state and metrics |
| `ContextAssembler` | Builds LLM input from system prompt, history and active turn |
| `LlmClient` | Sends generation requests to the LLM service |
| `TtsClient` | Sends speech and stop requests to the TTS service |
| `ModuleRuntimeRegistry` | Stores latest runtime state, health and details for UI modules |
| `ModuleRuntimeUpdater` | Applies runtime state transitions from events and health refreshes |
| UI controllers | Expose V2 UI HTTP endpoints |
| UI projection services | Build `TaiUiState` live snapshots |
| SSE publisher | Broadcasts live UI snapshots to connected clients |
| Debug controllers | Expose debug and observability endpoints |
| System health aggregator | Collects health status from configured Tai services for debug use |
| UI health refresh coordinator | Refreshes module health asynchronously for UI state |
| Actuator health | Exposes local orchestrator health |
| Swagger UI | Exposes OpenAPI endpoint documentation |
| Performance logger | Writes performance metrics |
| Conversation logger | Writes conversation history logs |

---

## Event model

The orchestrator separates external service facts from internal decisions.

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
| `SttTranscriptNoiseEvent` | none | Ignore noise for conversation turn creation |
| `LlmResponseCompletedEvent` | `AssistantReplyAcceptedEvent` | Attach assistant reply and request TTS |
| `LlmResponseFailedEvent` | `AssistantReplyFailedEvent` | Complete turn without assistant reply |
| `TtsPlaybackStartedEvent` | `AssistantSpeechStartedEvent` | Mark assistant as speaking |
| `TtsPlaybackCompletedEvent` | `AssistantSpeechCompletedEvent` | Complete assistant speech and finish turn |
| `TtsPlaybackFailedEvent` | `AssistantSpeechFailedEvent` | Complete turn after TTS failure |
| UI manual input | `UiManualTextInputReceivedEvent` | Accept typed user text and enter the normal user utterance flow |
| UI stop speak | `UiStopSpeakReceivedEvent` | Validate the active turn and request assistant flow interruption |

---

## Assistant interruption responsibility

Tai has two explicit assistant interruption paths:

- `UserSpeechStartedEvent` handles voice barge-in when the user starts speaking.
- `AssistantStopSpeakReceivedEvent` handles UI-requested interruption from the Stop Speak control.

They can handle:

- interruption of the current assistant flow when needed
- stopping TTS playback through `TtsClient` when speech is active
- marking the interrupted or superseded turn consistently
- updating the module runtime registry for UI rendering

Neither `UserSpeechStartedEvent` nor `AssistantStopSpeakReceivedEvent` creates a new user turn. A new user turn is created later by `UserUtteranceAcceptedEvent` when accepted user text is available.

`UserUtteranceAcceptedEvent` and `ClarificationRequestEvent` do not handle interruption. They focus on turn creation and LLM request orchestration.

```text
speech started
  → barge-in / stop current assistant output
  → wait for accepted transcript

UI Stop Speak
  → interrupt current assistant output or generation
  → no new user turn

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
| `turns` | Completed conversation history only |
| thinking state | Whether an LLM generation is in progress |
| speaking state | Whether TTS is preparing/speaking/silent |
| turn metrics map | Metrics keyed by correlation id |

The active turn is not present in `turns`. It is moved to `turns` only when it is explicitly completed or interrupted.

Conversation turns are persisted on explicit completion or interruption events only.

---

## Turn outcome

Completed turns carry a `TurnOutcome` before being stored in history and logged.

Typical outcomes:

| Outcome | Meaning |
|---|---|
| `COMPLETED` | The user/assistant turn completed normally |
| `INTERRUPTED` | The assistant output was interrupted, usually by barge-in |
| `SUPERSEDED` | A newer user turn superseded the active turn before it produced a usable assistant reply |
| `FAILED` | The turn failed because a required processing step failed |

The UI history endpoint exposes the stored turn outcome directly.

---

## Turn metrics

The orchestrator centralizes performance metrics by correlation id.

Metrics are updated by internal events and logged at turn completion.

Typical metric line:

```text
TURN metrics | correlationId=... totalTurnMs=... startedFrom=USER_SPEECH_STARTED userSpeechDurationMs=... transcriptDurationMs=... speechToTranscriptMs=... llmGenerationMs=... ttsSynthesisMs=... assistantFirstAudioLatencyMs=... ttsSpeechDurationMs=...
```

Current fields:

| Field | Meaning |
|---|---|
| `correlationId` | Correlation id of the turn |
| `totalTurnMs` | Duration from speech start or accepted utterance to metrics logging |
| `startedFrom` | Metrics start source: `USER_SPEECH_STARTED` or `USER_UTTERANCE_ACCEPTED` |
| `userSpeechDurationMs` | Duration of the user's captured speech segment reported by the accepted STT transcript event |
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

## V2 UI endpoints

The orchestrator owns the V2.0.0 UI projection until a dedicated UI gateway exists.

### Current snapshot

```http
GET /ui/state
```

Returns the current `TaiUiState` snapshot.

The endpoint reads the latest stored projection through `TaiUiStateProjectionService.currentOrRebuild()`.

### Live events

```http
GET /ui/events
```

Opens a Server-Sent Events stream.

The stream sends full UI snapshots using the event name:

```text
tai-ui-state
```

A snapshot is sent when the client connects and whenever the orchestrator refreshes the UI projection.

### Module details

```http
GET /ui/modules/{module}
```

Returns on-demand details for a module.

Module details are not included in the live snapshot. They are read from `ModuleRuntimeRegistry` and include the latest health, state, checked time, last correlation id, last error and module-specific health details.

### Conversation history

```http
GET /ui/history?limit=20
GET /ui/history?limit=20&cursor=<correlationId>
```

Returns completed conversation turns from newest to oldest.

The first page excludes the latest completed turn because it is already represented by the live overview. The cursor is the `correlationId` of the oldest item returned by the previous page.

### Manual input

```http
POST /ui/manual-input
```

Request:

```json
{
  "text": "string"
}
```

Response:

```json
{
  "accepted": true,
  "correlationId": "string",
  "acceptedAt": "2026-05-02T14:41:35.824Z"
}
```

Manual input publishes `UiManualTextInputReceivedEvent` through `TaiEventPublisher` and then enters the normal `UserUtteranceAcceptedEvent` flow.

### Stop speak

```http
POST /events/ui/stop-speak
```

Request:

```json
{
  "eventId": "generated-uuid",
  "occuredAt": "2026-05-02T14:41:35.824Z",
  "correlationId": "string",
  "source": "UI"
}
```

Response:

```text
No response body.
```

The endpoint receives a UI Stop Speak event. The handler validates that the requested `correlationId` matches the active turn before publishing `AssistantStopSpeakReceivedEvent`.

If the active turn is speaking, the orchestrator stops playback through `TtsClient.stop(correlationId)`, finalizes the active turn as `INTERRUPTED`, moves TTS runtime state back to idle and refreshes the UI projection.

If the active turn is thinking or generating, the orchestrator interrupts the active turn, moves LLM runtime state back to idle and refreshes the UI projection. No TTS stop request is sent in that case.

If there is no active turn or the requested `correlationId` is stale, the event has no conversation effect.

---

## TaiUiState live projection

`TaiUiState` is the live UI snapshot consumed by the V2 UI.

Main fields:

| Field | Meaning |
|---|---|
| `schemaVersion` | Snapshot schema version, currently `2.0` |
| `sequence` | Monotonic sequence incremented on projection rebuild |
| `generatedAt` | Projection generation timestamp |
| `conversationStatus` | User-facing global conversation status |
| `modules` | Map of `TaiModule` to compact `ModuleOverview` |
| `lastUserUtterance` | Latest user text from active or completed turns |
| `lastAssistantUtterance` | Latest displayable assistant text |

`conversationStatus` values:

| Status | Meaning |
|---|---|
| `IDLE` | Tai is not actively listening, thinking or speaking |
| `LISTENING` | STT listener is listening or capturing |
| `THINKING` | Tai is processing, transcribing or generating |
| `SPEAKING` | TTS is synthesizing or speaking |
| `ERROR` | A core module is down or in error activity |

`lastAssistantUtterance` intentionally represents the latest assistant text useful for subtitles. A failed turn with no assistant text does not replace the previous displayable assistant utterance.

---

## Module runtime registry

The UI module overview is driven by `ModuleRuntimeRegistry`.

Tracked modules:

- `SYSTEM`
- `ORCHESTRATOR`
- `STT_LISTENER`
- `STT_WHISPER`
- `LLM`
- `TTS_PIPER`
- `UI_GATEWAY`
- `AVATAR`

Health values:

- `UP`
- `DEGRADED`
- `DOWN`
- `DISABLED`

Runtime activities:

- `UNKNOWN`
- `DISABLED`
- `IDLE`
- `LISTENING`
- `CAPTURING`
- `PROCESSING`
- `GENERATING`
- `SYNTHESIZING`
- `SPEAKING`
- `ERROR`

Runtime state is updated by internal events. Health details are updated by asynchronous health checks.

`STT_WHISPER` is not inferred from generic STT events. Until dedicated Whisper runtime events exist, it is primarily updated from health refreshes.

---

## UI module health refresh

The V2 UI health refresh system runs independently from live event handling.

Key rules:

- inbound service events do not trigger health calls
- the UI snapshot builder does not trigger health calls
- a scheduler refreshes stale module health asynchronously
- a module entering runtime `ERROR` can request an immediate asynchronous health refresh
- health refresh results update `ModuleRuntimeRegistry`
- health refresh results request a UI projection refresh
- repeated UI refresh requests are coalesced

Health refresh uses an HTTP/1.1 `RestTemplate` and reads Actuator health responses even when they use non-2xx status codes.

Important health mapping rules:

| Source condition | UI mapping |
|---|---|
| HTTP call unreachable / connection refused | `DOWN` |
| Actuator `UP` | `UP` |
| Actuator `OUT_OF_SERVICE` | `DEGRADED` |
| Health result `DOWN` | activity `ERROR` |
| `STT_LISTENER` health `DEGRADED` | activity `IDLE` |

`stale` in `ModuleOverview` is based on `lastHealthAt`, not on the latest activity timestamp. `SYSTEM`, `ORCHESTRATOR` and disabled modules are not marked stale.

---

## UI refresh events and SSE publication

Runtime handlers, health refreshes and UI actions request a projection refresh through a Spring event:

```text
UiStateRefreshRequestedEvent
```

The refresh listener:

```text
UiStateRefreshRequestedEvent
  → debounce/coalesce requests
  → TaiUiStateProjectionService.rebuild()
  → TaiUiStateStore update
  → TaiUiStatePushSink.push(...)
  → SSE broadcast
```

A shutdown guard prevents late refresh scheduling while the application context is closing.

---

## Debug and observability endpoints

### Actuator health

```http
GET /actuator/health
```

Returns the local health status of the orchestrator process.

### Swagger UI

```text
http://localhost:8080/docs
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

### System health debug endpoint

```http
GET /debug/system/health
```

Returns a consolidated debug view of configured Tai services.

The response contains one entry per configured service:

- service status
- health endpoint URL
- response time
- error message when the health check fails

Example:

```json
{
  "status": "DEGRADED",
  "checkedAt": "2026-04-28T10:15:30.123Z",
  "services": {
    "orchestrator": {
      "status": "UP",
      "url": "http://localhost:8080/actuator/health",
      "responseTimeMs": 8,
      "errorMessage": null
    },
    "sttWhisper": {
      "status": "TIMEOUT",
      "url": "http://localhost:8095/health",
      "responseTimeMs": 2001,
      "errorMessage": "request timed out"
    }
  }
}
```

The global status is `UP` when all configured services are up. It is `DEGRADED` when at least one configured service is unavailable, timed out, or reports a non-up status.

---

## Voice input flow

```text
SttSpeechStartedEvent
  → UserSpeechStartedEvent
  → optional barge-in
  → runtime registry update
  → UI state refresh requested

SttTranscriptAcceptedEvent
  → UserUtteranceAcceptedEvent
  → active turn created
  → context assembled
  → LLM request sent
  → runtime registry update
  → UI state refresh requested

LlmResponseCompletedEvent
  → AssistantReplyAcceptedEvent
  → assistant reply attached
  → TTS request sent if enabled
  → runtime registry update
  → UI state refresh requested

TtsPlaybackStartedEvent
  → AssistantSpeechStartedEvent
  → runtime registry update
  → UI state refresh requested

TtsPlaybackCompletedEvent
  → AssistantSpeechCompletedEvent
  → ConversationTurnCompletedEvent
  → turn persisted
  → metrics logged
  → runtime registry update
  → UI state refresh requested
```

---

## Manual text input flow

```text
POST /ui/manual-input
  → UiManualTextInputReceivedEvent
  → UserUtteranceAcceptedEvent
  → active turn created
  → context assembled
  → LLM request sent
  → normal assistant response flow
```

Manual text input does not involve `SttSpeechStartedEvent`.

---

## UI Stop Speak flow

```text
POST /events/ui/stop-speak
  → UiStopSpeakReceivedEvent
  → active turn correlation checked
  → AssistantStopSpeakReceivedEvent when the active turn matches
  → interrupt speaking or generating assistant flow
  → UI state refresh requested
```

UI Stop Speak does not create a new user turn and does not enter the `UserUtteranceAcceptedEvent` flow.

---

## LLM failure flow

```text
UserUtteranceAcceptedEvent
  → LLM request sent

LlmResponseFailedEvent
  → AssistantReplyFailedEvent
  → ConversationTurnCompletedEvent
  → turn finalized without assistant reply
  → runtime registry marks LLM as degraded/error
  → UI keeps the latest displayable assistant subtitle
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
  → runtime registry marks TTS as degraded/error
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

Ollama generation is not cancelled from the client side. The runtime registry therefore keeps the LLM as generating until a later event gives a more accurate state.

---

## UI Stop Speak while LLM is generating

```text
first user utterance
  → LLM generation starts

POST /events/ui/stop-speak
  → UiStopSpeakReceivedEvent
  → AssistantStopSpeakReceivedEvent
  → active turn interrupted
  → LLM runtime state becomes idle

late LlmResponseCompletedEvent
  → ignored as stale
```

The UI interruption does not create a replacement user turn. It only stops the current assistant flow.

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
  → TTS runtime state becomes silent/idle

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
  → STT listener runtime returns to listening/idle as appropriate
```

Noise does not create a conversation turn.

---

## State transitions summary

| Phase | Event | State change |
|---|---|---|
| User speech starts | `UserSpeechStartedEvent` | interrupt active assistant flow if needed |
| UI Stop Speak | `AssistantStopSpeakReceivedEvent` | interrupt active speaking or generating assistant flow if the active turn matches |
| Accepted input | `UserUtteranceAcceptedEvent` | thinking → generating |
| Clarification needed | `ClarificationRequestEvent` | thinking → generating |
| LLM completed | `LlmResponseCompletedEvent` | thinking → idle |
| TTS requested | `AssistantReplyAcceptedEvent` | speaking → preparing |
| Speaking start | `AssistantSpeechStartedEvent` | speaking → speaking |
| Speaking end | `AssistantSpeechCompletedEvent` | speaking → silent |
| Turn completion | `ConversationTurnCompletedEvent` | active turn → history |
| Health refresh | module health response | update module health/details and refresh UI projection |

---

## Configuration

Relevant orchestrator properties:

```yaml
tai:
  llm:
    base-url: http://localhost:8092
  tts:
    base-url: http://localhost:8093
  system-health:
    services:
      orchestrator:
        url: http://localhost:8080/actuator/health
      stt-listener:
        url: http://localhost:8094/actuator/health
      stt-whisper:
        url: http://localhost:8095/health
      llm:
        url: http://localhost:8092/actuator/health
      tts-piper:
        url: http://localhost:8093/actuator/health
    connect-timeout-ms: 1000
    read-timeout-ms: 2000
  ui:
    health-refresh:
      enabled: true
      interval: 5s
      freshness-threshold: 15s
      request-timeout: 2s
      error-refresh-min-delay: 3s
      min-delay-between-attempts: 1s
      ui-push-debounce: 75ms
      endpoints:
        orchestrator: http://localhost:8080/actuator/health
        stt-listener: http://localhost:8094/actuator/health
        stt-whisper: http://localhost:8095/health
        llm: http://localhost:8092/actuator/health
        tts-piper: http://localhost:8093/actuator/health
    state-refresh:
      enabled: true
    manual-input:
      max-length: 2000
    sse:
      timeout: 1h
      reconnect-delay: 2s
```

| Property | Description |
|---|---|
| `tai.llm.base-url` | Base URL of the LLM service |
| `tai.tts.base-url` | Base URL of the TTS service |
| `tai.system-health.services.*.url` | Debug health aggregation endpoints |
| `tai.ui.health-refresh.enabled` | Enables asynchronous UI module health refresh |
| `tai.ui.health-refresh.interval` | Scheduler interval used to look for stale health data |
| `tai.ui.health-refresh.freshness-threshold` | Max age before module health is considered stale |
| `tai.ui.health-refresh.request-timeout` | Timeout for UI health HTTP calls |
| `tai.ui.health-refresh.error-refresh-min-delay` | Minimum delay between error-triggered refresh attempts |
| `tai.ui.health-refresh.min-delay-between-attempts` | Minimum delay between refresh attempts for one module |
| `tai.ui.health-refresh.endpoints.*` | Health endpoints used by the UI runtime registry |
| `tai.ui.state-refresh.enabled` | Enables Spring event based UI projection rebuilds |
| `tai.ui.manual-input.max-length` | Maximum accepted manual input text length |
| `tai.ui.sse.timeout` | SSE emitter timeout |
| `tai.ui.sse.reconnect-delay` | Browser reconnection delay advertised in SSE events |

---

## Logging

The orchestrator writes dedicated logs for different concerns.

| Logger | Purpose | Location |
|---|---|---|
| conversation logger | Conversation turns and assistant/user content | File: *-conversation.log |
| performance logger | Turn-level performance metrics | File: *-performance.log |
| error logger | Metric overwrite issues and dedicated error events | Console |
| context logger | Changes in the context | Console |
| decision logger | Decisions made, such as ignoring stale events | Console |
| trace logger | Technical low level tracing | Console |

The log directory is configured through application properties.

---

## Shutdown behavior

The UI refresh and health refresh infrastructure uses asynchronous executors and schedulers.

A shutdown guard prevents late health refresh results from scheduling UI projection rebuilds after the Spring context has started closing. This keeps local shutdown quiet and avoids noisy `TaskRejectedException` logs from late background tasks.

---

## Key design principles

- Events represent facts, not intentions.
- One event has one responsibility.
- Inbound events reflect external service callbacks.
- Internal events model orchestrator decisions.
- Conversation state changes happen inside internal event handlers.
- Voice barge-in is owned by `UserSpeechStartedEvent`.
- UI-requested assistant interruption is owned by `AssistantStopSpeakReceivedEvent`.
- Conversation turns are persisted only on completion, interruption or failure.
- The active turn is not part of completed history.
- UI live state is a projection, not a source of truth.
- UI snapshots do not trigger health calls or external service commands.
- On-demand UI endpoints are kept separate from live snapshots.
