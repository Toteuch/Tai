# Tai Orchestrator V1 — Event Flow Documentation

## Overview

This document describes the event flows handled by the Tai Orchestrator V1.

The orchestrator follows an event-driven lifecycle:

1. Inbound event received
2. Internal event published
3. Handler applies one focused decision
4. External service command is sent when needed
5. Completion events finalize the flow

The STT, LLM and TTS services are externalized. The orchestrator reacts to their callbacks and owns the conversation
decisions.

---

## Event responsibility split

### STT inbound events

| Inbound event                      | Internal event               | Responsibility                                         |
|------------------------------------|------------------------------|--------------------------------------------------------|
| `SttSpeechStartedEvent`            | `UserSpeechStartedEvent`     | Handle immediate user barge-in                         |
| `SttTranscriptAcceptedEvent`       | `UserUtteranceAcceptedEvent` | Create a new user turn and call the LLM                |
| `SttTranscriptUnintelligibleEvent` | `ClarificationRequestEvent`  | Create a temporary clarification turn and call the LLM |
| `SttTranscriptNoiseEvent`          | none or ignored flow         | Ignore noise / no conversation turn                    |

### Important design rule

`UserSpeechStartedEvent` is the only internal event responsible for barge-in.

It handles:

- interruption of the current assistant flow when needed
- stopping TTS playback through `TtsClient` when needed
- marking the interrupted/superseded turn consistently

`UserUtteranceAcceptedEvent` and `ClarificationRequestEvent` only handle their own conversation turn creation and LLM
call.

This keeps the responsibilities clean:

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

## Scenario 1 — Voice Input → Full Assistant Response

### Flow

1. `inbound`:`SttSpeechStartedEvent`  
   → user started speaking
2. `internal`:`UserSpeechStartedEvent`  
   → no assistant flow is active, so no barge-in is required
3. `inbound`:`SttTranscriptAcceptedEvent`  
   → validated transcription received from STT
4. `internal`:`UserUtteranceAcceptedEvent`  
   → user turn created and set as `activeTurn`  
   → context assembled and sent to LLM
5. `inbound`:`LlmResponseCompletedEvent`  
   → LLM response received
6. `internal`:`AssistantReplyAcceptedEvent`  
   → assistant reply validated and attached to `activeTurn`  
   → request for audio synthesis and playback *(if TTS enabled)*
7. `inbound`:`TtsPlaybackStartedEvent` *(if TTS enabled)*  
   → audio playback has started
8. `internal`:`AssistantSpeechStartedEvent` *(if TTS enabled)*  
   → assistant enters speaking state
9. `inbound`:`TtsPlaybackCompletedEvent` *(if TTS enabled)*  
   → audio playback has finished
10. `internal`:`AssistantSpeechCompletedEvent` *(if TTS enabled)*  
    → assistant finished speaking
11. `internal`:`ConversationTurnCompletedEvent`  
    → turn finalized and persisted in history

---

### Notes

- For a normal voice input while Tai is idle, `UserSpeechStartedEvent` does not interrupt anything.
- If TTS is disabled, TTS playback events are skipped:

```text
AssistantReplyAcceptedEvent → ConversationTurnCompletedEvent
```

- `ConversationTurnCompletedEvent` is the only point where a turn is persisted into history.
- `SessionContext` structure:
    - `activeTurn` → current in-progress turn
    - `turns` → finalized conversation history
- `ContextAssembler` builds LLM input from:
    - system prompt
    - `turns` history
    - `activeTurn` if present

---

## Scenario 2 — Manual Text Input → Full Assistant Response

### Flow

1. `inbound`:`UiManualTextInputReceivedEvent`  
   → user text input received from UI
2. `internal`:`UserUtteranceAcceptedEvent`  
   → user turn created and set as `activeTurn`  
   → context assembled and sent to LLM
3. `inbound`:`LlmResponseCompletedEvent`  
   → LLM response received
4. `internal`:`AssistantReplyAcceptedEvent`  
   → assistant reply validated and attached to `activeTurn`  
   → request for audio synthesis and playback *(if TTS enabled)*
5. `inbound`:`TtsPlaybackStartedEvent` *(if TTS enabled)*  
   → audio playback has started
6. `internal`:`AssistantSpeechStartedEvent` *(if TTS enabled)*  
   → assistant enters speaking state
7. `inbound`:`TtsPlaybackCompletedEvent` *(if TTS enabled)*  
   → audio playback has finished
8. `internal`:`AssistantSpeechCompletedEvent` *(if TTS enabled)*  
   → assistant finished speaking
9. `internal`:`ConversationTurnCompletedEvent`  
   → turn finalized and persisted in history

---

### Notes

- This scenario is identical to the voice flow, except that the input originates from the UI instead of STT.
- No `SttSpeechStartedEvent` is involved in manual text input.
- This scenario is more about debugging than prod usage.
- This scenario doesn't support BargeIn

---

## Scenario 3 — Voice Input → LLM Failure → Graceful Turn Completion

### Flow

1. `inbound`:`SttSpeechStartedEvent`  
   → user started speaking
2. `internal`:`UserSpeechStartedEvent`  
   → no assistant flow is active, so no barge-in is required
3. `inbound`:`SttTranscriptAcceptedEvent`  
   → validated transcription received from STT
4. `internal`:`UserUtteranceAcceptedEvent`  
   → user turn created and set as `activeTurn`  
   → context assembled and sent to LLM
5. `inbound`:`LlmResponseFailedEvent`  
   → LLM call failed
6. `internal`:`AssistantReplyFailedEvent`  
   → failure handled at assistant level
7. `internal`:`ConversationTurnCompletedEvent`  
   → turn finalized without assistant reply and persisted in history

---

### Notes

- No TTS events are triggered because no assistant reply is generated.
- The turn is still persisted, allowing the system to maintain conversational continuity.
- `activeTurn` is created but may not contain an assistant response.
- This scenario prevents the conversation pipeline from being blocked by LLM errors.
- TODO: request a TTS speech saying there is a problem with Tai's AI

---

## Scenario 4 — Voice Input → TTS Failure → Graceful Turn Completion

### Flow

1. `inbound`:`SttSpeechStartedEvent`  
   → user started speaking
2. `internal`:`UserSpeechStartedEvent`  
   → no assistant flow is active, so no barge-in is required
3. `inbound`:`SttTranscriptAcceptedEvent`  
   → validated transcription received from STT
4. `internal`:`UserUtteranceAcceptedEvent`  
   → user turn created and set as `activeTurn`  
   → context assembled and sent to LLM
5. `inbound`:`LlmResponseCompletedEvent`  
   → LLM response received
6. `internal`:`AssistantReplyAcceptedEvent`  
   → assistant reply validated and attached to `activeTurn`  
   → request for audio synthesis and playback
7. `inbound`:`TtsPlaybackStartedEvent`  
   → TTS playback process started
8. `internal`:`AssistantSpeechStartedEvent`  
   → assistant enters speaking state
9. `inbound`:`TtsPlaybackFailedEvent`  
   → TTS synthesis or playback failed
10. `internal`:`AssistantSpeechFailedEvent`  
    → assistant speech failure handled
11. `internal`:`ConversationTurnCompletedEvent`  
    → turn finalized and persisted in history

---

### Notes

- The assistant reply is still attached to the current turn before TTS starts.
- Even if speech synthesis or playback fails, the conversation turn is completed.
- The turn is persisted with the assistant text, even if spoken output failed.
- Speech state is reset after failure.
- TODO: Add a system prompt in history saying that the previous response wasn't completed/failed

---

## Scenario 5 — User Starts Speaking While LLM Is Generating

### Flow

1. `inbound`:`SttTranscriptAcceptedEvent` *(first turn)*  
   → first validated transcription received from STT
2. `internal`:`UserUtteranceAcceptedEvent` *(first turn)*  
   → first user turn created and set as `activeTurn`  
   → context assembled and sent to LLM
3. `inbound`:`SttSpeechStartedEvent` *(second user input)*  
   → user starts speaking again while the LLM is still generating
4. `internal`:`UserSpeechStartedEvent` *(second user input)*  
   → current active turn is superseded before assistant reply  
   → previous turn is preserved with user text only  
   → no TTS stop is needed because TTS is not speaking yet
5. `inbound`:`SttTranscriptAcceptedEvent` *(second turn)*  
   → second validated transcription received from STT
6. `internal`:`UserUtteranceAcceptedEvent` *(second turn)*  
   → second user turn created and set as `activeTurn`  
   → context assembled and sent to LLM
7. `inbound`:`LlmResponseCompletedEvent` *(first turn)*  
   → first LLM response is received after the turn has already been superseded
8. `handler`:`LlmResponseCompletedEventHandler` *(first turn)*  
   → stale LLM response is ignored because it no longer matches `activeTurn`
9. `inbound`:`LlmResponseCompletedEvent` *(second turn)*  
   → second LLM response is received for the current active turn
10. `internal`:`AssistantReplyAcceptedEvent` *(second turn)*  
    → second assistant reply is accepted and attached to `activeTurn`  
    → request for audio synthesis and playback
11. `inbound`:`TtsPlaybackStartedEvent` *(second turn)*  
    → audio playback has started
12. `internal`:`AssistantSpeechStartedEvent` *(second turn)*  
    → assistant enters speaking state
13. `inbound`:`TtsPlaybackCompletedEvent` *(second turn)*  
    → audio playback has finished
14. `internal`:`AssistantSpeechCompletedEvent` *(second turn)*  
    → assistant finished speaking
15. `internal`:`ConversationTurnCompletedEvent` *(second turn)*  
    → second turn finalized and persisted in history

---

### Notes

- This scenario validates interruption while the LLM is still generating.
- Barge-in is triggered by `UserSpeechStartedEvent`, not by `UserUtteranceAcceptedEvent`.
- The first LLM call is not canceled, but its response is safely ignored when it comes back late.
- Only the latest active turn can produce an assistant reply and trigger TTS.
- The stale first turn must not trigger:
    - `AssistantReplyAcceptedEvent`
    - `TtsPlaybackStartedEvent`
    - `AssistantSpeechStartedEvent`
    - `ConversationTurnCompletedEvent`
- `SessionContext` behavior:
    - the first `activeTurn` is superseded when speech starts
    - the first turn is added to history only with user text
    - `supersededBeforeAssistantReply` is set to `true`
    - stale LLM responses are rejected by correlation ID
- This protects the conversation from outdated assistant replies when the user starts speaking before Tai has finished
  thinking.

---

## Scenario 6 — User Starts Speaking While TTS Is Preparing or Speaking

### Flow

1. `inbound`:`SttTranscriptAcceptedEvent` *(first turn)*  
   → first validated transcription received from STT
2. `internal`:`UserUtteranceAcceptedEvent` *(first turn)*  
   → first user turn created and set as `activeTurn`  
   → context assembled and sent to LLM
3. `inbound`:`LlmResponseCompletedEvent` *(first turn)*  
   → first LLM response is received for the current active turn
4. `internal`:`AssistantReplyAcceptedEvent` *(first turn)*  
   → first assistant reply is accepted and attached to `activeTurn`  
   → request for audio synthesis and playback
5. `inbound`:`TtsPlaybackStartedEvent` *(first turn)*  
   → audio playback has started
6. `internal`:`AssistantSpeechStartedEvent` *(first turn)*  
   → assistant enters speaking state
7. `inbound`:`SttSpeechStartedEvent` *(second user input)*  
   → user starts speaking while TTS is preparing or speaking
8. `internal`:`UserSpeechStartedEvent` *(second user input)*  
   → active assistant output is interrupted  
   → `TtsClient.stop(...)` is called when necessary  
   → first turn is finalized with assistant reply and interruption metadata
9. `inbound`:`SttTranscriptAcceptedEvent` *(second turn)*  
   → second validated transcription received from STT
10. `internal`:`UserUtteranceAcceptedEvent` *(second turn)*  
    → second user turn created and set as `activeTurn`  
    → context assembled and sent to LLM
11. `inbound`:`LlmResponseCompletedEvent` *(second turn)*  
    → second LLM response is received for the current active turn
12. `internal`:`AssistantReplyAcceptedEvent` *(second turn)*  
    → second assistant reply is accepted and attached to `activeTurn`  
    → request for audio synthesis and playback
13. `inbound`:`TtsPlaybackStartedEvent` *(second turn)*  
    → audio playback has started
14. `internal`:`AssistantSpeechStartedEvent` *(second turn)*  
    → assistant enters speaking state
15. `inbound`:`TtsPlaybackCompletedEvent` *(second turn)*  
    → audio playback has finished
16. `internal`:`AssistantSpeechCompletedEvent` *(second turn)*  
    → assistant finished speaking
17. `internal`:`ConversationTurnCompletedEvent` *(second turn)*  
    → second turn finalized and persisted in history

---

### Notes

- This scenario validates barge-in during TTS preparation or playback.
- Barge-in is triggered as soon as speech starts, not when the final transcript arrives.
- `UserSpeechStartedEvent` owns the interruption logic and TTS stop command.
- `UserUtteranceAcceptedEvent` only creates the next turn and calls the LLM after the transcript is accepted.
- `SessionContext` behavior:
    - the first active turn is interrupted by `UserSpeechStartedEvent`
    - the first turn is added to history with `assistantPlaybackInterrupted` set to `true`
    - the second turn can use the interrupted assistant reply as context
- This allows Tai to be aware that she was interrupted when generating the next assistant reply.

---

## Scenario 7 — User Speech Starts but Final Transcript Is Unintelligible

### Flow

1. `inbound`:`SttSpeechStartedEvent`  
   → user starts speaking
2. `internal`:`UserSpeechStartedEvent`  
   → current assistant flow is interrupted if needed
3. `inbound`:`SttTranscriptUnintelligibleEvent`  
   → STT could not produce an accepted transcript
4. `internal`:`ClarificationRequestEvent`  
   → temporary clarification turn created with `keepInHistory=false`  
   → short-lived clarification prompt sent to LLM
5. `inbound`:`LlmResponseCompletedEvent`  
   → LLM clarification response received
6. `internal`:`AssistantReplyAcceptedEvent`  
   → clarification reply attached to `activeTurn`  
   → request for TTS if enabled
7. `inbound`:`TtsPlaybackStartedEvent` *(if TTS enabled)*  
   → audio playback has started
8. `internal`:`AssistantSpeechStartedEvent` *(if TTS enabled)*  
   → assistant enters speaking state
9. `inbound`:`TtsPlaybackCompletedEvent` *(if TTS enabled)*  
   → audio playback has finished
10. `internal`:`AssistantSpeechCompletedEvent` *(if TTS enabled)*  
    → assistant finished speaking
11. `internal`:`ConversationTurnCompletedEvent`  
    → clarification turn finalized and not persisted in history

---

### Notes

- `UserSpeechStartedEvent` may interrupt an active assistant output even if the final transcript is unintelligible.
- `ClarificationRequestEvent` only creates the clarification turn and calls the LLM.
- Once the clarification turn is completed, the normal flow resumes with the conversation context from before the
  unintelligible input.

---

## Scenario 8 — STT Noise After Speech Started

### Flow

1. `inbound`:`SttSpeechStartedEvent`  
   → listener detected audio above speech threshold
2. `internal`:`UserSpeechStartedEvent`  
   → current assistant flow is interrupted if needed
3. `inbound`:`SttTranscriptNoiseEvent`  
   → final STT decision classifies the segment as noise
4. No conversation turn is created.

---

### Notes

- A loud noise can trigger `speechStarted`, because it crosses the capture threshold.
- The final STT callback can still classify the segment as noise.
- This means an assistant output may be interrupted by noise if the noise crosses the speech-start threshold.
- This is acceptable for now because `speechStarted` is intentionally early and conservative.
- Later tuning can reduce false positives in the listener/gatekeeper.

---

## State Transitions Summary

| Phase                | Event                            | State Change                              |
|----------------------|----------------------------------|-------------------------------------------|
| User speech starts   | `UserSpeechStartedEvent`         | interrupt active assistant flow if needed |
| Accepted input       | `UserUtteranceAcceptedEvent`     | thinking → generating                     |
| Clarification needed | `ClarificationRequestEvent`      | thinking → generating                     |
| LLM completed        | `LlmResponseCompletedEvent`      | thinking → idle                           |
| TTS requested        | `AssistantReplyAcceptedEvent`    | speaking → preparing                      |
| Speaking start       | `AssistantSpeechStartedEvent`    | speaking → speaking                       |
| Speaking end         | `AssistantSpeechCompletedEvent`  | speaking → silent                         |
| Turn completion      | `ConversationTurnCompletedEvent` | active turn → history                     |

---

## Key Design Principles

- Events represent facts, not intentions.
- One event = one responsibility.
- Internal events structure the flow.
- Inbound events reflect real-world changes.
- Orchestrator owns all conversation decisions.
