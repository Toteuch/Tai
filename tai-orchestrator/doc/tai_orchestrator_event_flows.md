# Tai Orchestrator V1 — Event Flow Documentation

## Overview

This document describes the event flows handled by the Tai Orchestrator V1.

The orchestrator follows an event-driven lifecycle:

1. Input event received
2. Internal processing events triggered
3. External services called
4. Completion events finalize the flow

---

## Scenario 1 --- Voice Input → Full Assistant Response

### Flow

1. `inbound`:`SttTranscriptAcceptedEvent`\
   → validated transcription received from STT
2. `internal`:`UserUtteranceAcceptedEvent`\
   → user turn created and set as `activeTurn`\
   → context assembled and sent to LLM
3. `inbound`:`LlmResponseCompletedEvent`\
   → LLM response received
4. `internal`:`AssistantReplyAcceptedEvent`\
   → assistant reply validated and attached to `activeTurn`\
   → request for audio synthesis and playback *(if TTS enabled)*
5. `inbound`:`TtsPlaybackStartedEvent` *(if TTS enabled)*\
   → audio playback has started
6. `internal`:`AssistantSpeechStartedEvent` *(if TTS enabled)*\
   → assistant enters speaking state
7. `inbound`:`TtsPlaybackCompletedEvent` *(if TTS enabled)*\
   → audio playback has finished
8. `internal`:`AssistantSpeechCompletedEvent` *(if TTS enabled)*\
   → assistant finished speaking
9. `internal`:`ConversationTurnCompletedEvent`\
   → turn finalized and persisted in history

------------------------------------------------------------------------

### Notes

- If TTS is disabled, steps 5 → 8 are skipped:

AssistantReplyAcceptedEvent → ConversationTurnCompletedEvent

- `ConversationTurnCompletedEvent` is the **only point where a turn is persisted** into history.
- `SessionContext` structure:
    - `activeTurn` → current in-progress turn
    - `turns` → finalized conversation history
- `ContextAssembler` builds LLM input from:
    - system prompt
    - `turns` (history)
    - `activeTurn` (if present)

---

## Scenario 2 --- Manual Text Input → Full Assistant Response

### Flow

1. `inbound`:`UiManualTextInputReceivedEvent`\
   → user text input received from UI
2. `internal`:`UserUtteranceAcceptedEvent`\
   → user turn created and set as `activeTurn`\
   → context assembled and sent to LLM
3. `inbound`:`LlmResponseCompletedEvent`\
   → LLM response received
4. `internal`:`AssistantReplyAcceptedEvent`\
   → assistant reply validated and attached to `activeTurn`\
   → request for audio synthesis and playback *(if TTS enabled)*
5. `inbound`:`TtsPlaybackStartedEvent` *(if TTS enabled)*\
   → audio playback has started
6. `internal`:`AssistantSpeechStartedEvent` *(if TTS enabled)*\
   → assistant enters speaking state
7. `inbound`:`TtsPlaybackCompletedEvent` *(if TTS enabled)*\
   → audio playback has finished
8. `internal`:`AssistantSpeechCompletedEvent` *(if TTS enabled)*\
   → assistant finished speaking
9. `internal`:`ConversationTurnCompletedEvent`\
   → turn finalized and persisted in history

------------------------------------------------------------------------

### Notes

- This scenario is identical to the voice flow, except that the input
  originates from the UI instead of STT.

---

## Scenario 3 --- Voice Input → LLM Failure → Graceful Turn Completion

### Flow

1. `inbound`:`SttTranscriptAcceptedEvent`\
   → validated transcription received from STT
2. `internal`:`UserUtteranceAcceptedEvent`\
   → user turn created and set as `activeTurn`\
   → context assembled and sent to LLM
3. `inbound`:`LlmResponseFailedEvent`\
   → LLM call failed (timeout, error, or invalid response)
4. `internal`:`AssistantReplyFailedEvent`\
   → failure handled at assistant level (no valid reply produced)
5. `internal`:`ConversationTurnCompletedEvent`\
   → turn finalized without assistant reply and persisted in history

------------------------------------------------------------------------

### Notes

- No TTS events are triggered in this scenario since no assistant
  reply is generated.

- The system gracefully terminates the turn even in case of LLM
  failure.

- The turn is still persisted, allowing the system to maintain
  conversational continuity.

- `SessionContext` behavior:

    - `activeTurn` is created but may not contain an assistant
      response
    - the turn is still added to `turns` upon completion

- This scenario ensures system robustness and prevents blocking the
  conversation pipeline on LLM errors.

---

## Scenario 4 — Voice Input → TTS Failure → Graceful Turn Completion

### Flow

1. `inbound`:`SttTranscriptAcceptedEvent`  
   → validated transcription received from STT
2. `internal`:`UserUtteranceAcceptedEvent`  
   → user turn created and set as `activeTurn`\
   → context assembled and sent to LLM
3. `inbound`:`LlmResponseCompletedEvent`  
   → LLM response received
4. `internal`:`AssistantReplyAcceptedEvent`  
   → assistant reply validated and attached to `activeTurn`
5. `outbound`:`TtsPlaybackRequestedEvent`  
   → request for audio synthesis and playback
6. `inbound`:`TtsPlaybackStartedEvent`  
   → TTS playback process started
7. `inbound`:`TtsPlaybackFailedEvent`  
   → TTS synthesis or playback failed
8. `internal`:`AssistantSpeechFailedEvent`  
   → assistant speech failure handled
9. `internal`:`ConversationTurnCompletedEvent`  
   → turn finalized and persisted in history

---

### Notes

- The assistant reply is still attached to the current turn before TTS starts.

- Even if speech synthesis or playback fails, the conversation turn is completed.

- The turn is persisted with the assistant text, even if the spoken output failed.

- This prevents the conversation pipeline from being blocked by TTS failures.

- `SessionContext` behavior:
    - `activeTurn` contains both user text and assistant reply
    - speech state is reset after failure
    - the turn is added to `turns` when `ConversationTurnCompletedEvent` is handled

---

## Scenario 5 — User Interrupts While LLM Is Generating

### Flow

1. `inbound`:`SttTranscriptAcceptedEvent` *(first turn)*  
   → first validated transcription received from STT
2. `internal`:`UserUtteranceAcceptedEvent` *(first turn)*  
   → first user turn created and set as `activeTurn`\
   → context assembled and sent to LLM
3. `inbound`:`SttTranscriptAcceptedEvent` *(second turn)*  
   → second validated transcription received before the first LLM response completes
4. `internal`:`UserUtteranceAcceptedEvent` *(second turn)*  
   → first turn is superseded and second user turn becomes the new `activeTurn`\
   → context assembled and sent to LLM (including first user message and a system prompt)
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

- The first LLM call is not cancelled, but its response is safely ignored when it comes back late.

- Only the latest active turn is allowed to produce an assistant reply and trigger TTS.

- The stale first turn must not trigger:
    - `AssistantReplyAcceptedEvent`
    - `TtsPlaybackStartedEvent`
    - `AssistantSpeechStartedEvent`
    - `ConversationTurnCompletedEvent`

- `SessionContext` behavior:
    - the first `activeTurn` is superseded by the second user input
    - the first `activeTurn` is added to turns only with the user text with `supersededBeforeAssistantReply` set to true
    - stale LLM responses are rejected by correlation ID

- This protects the conversation from outdated assistant replies when the user speaks again before Tai has finished
  thinking.

---

## Scenario 6 — Interruption (Barge-in) during TTS Speaking/Synthetizing

### Flow

1. `inbound`:`SttTranscriptAcceptedEvent` *(first turn)*  
   → first validated transcription received from STT
2. `internal`:`UserUtteranceAcceptedEvent` *(first turn)*  
   → first user turn created and set as `activeTurn`\
   → context assembled and sent to LLM
3. `inbound`:`LlmResponseCompletedEvent` *(first turn)*  
   → first LLM response is received for the current active turn
4. `internal`:`AssistantReplyAcceptedEvent` *(first turn)*  
   → first assistant reply is accepted and attached to `activeTurn`\
   → request for audio synthesis and playback
5. `inbound`:`TtsPlaybackStartedEvent` *(first turn)*  
   → audio playback has started
6. `internal`:`AssistantSpeechStartedEvent` *(first turn)*  
   → assistant enters speaking state
7. `inbound`:`SttTranscriptAcceptedEvent` *(second turn)*  
   → second validated transcription received before the Tts playback is completed
8. `internal`:`UserUtteranceAcceptedEvent` *(second turn)*  
   → Tts speech is stopped and turn is added with user text, assistant response and assistantPlaybackInterrupted set to
   true\
   → second user turn created and set as `activeTurn`\
   → context assembled and sent to LLM (including first user message, assistant reply and a system prompt)
9. `inbound`:`LlmResponseCompletedEvent` *(second turn)*  
   → second LLM response is received for the current active turn
10. `internal`:`AssistantReplyAcceptedEvent` *(second turn)*  
    → second assistant reply is accepted and attached to `activeTurn`\
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

- This scenario validates interruption while the TTS is still speaking.

- Tts is stopped by the `UserUtteranceAcceptedEvent` handler, meaning the user must have finished its sentence

- `SessionContext` behavior:
    - the first `activeTurn` is stopped by the second user input
    - the first `activeTurn` is added to turns with `assistantPlaybackInterrupted` set to true

- This allows Tai to be aware that she's been interrupted when generating the second turn's assistant reply.

---

## Scenario 8 — STT Transcription Unclear

### Flow

1. `inbound`:`SttTranscriptUnintelligbleEvent`   
   → unintelligible (yet not noise) transcription received from STT (without text)
2. `internal`:`ClarificationRequestEvent`   
   → conversation turn created with `keepInHistory` set to `false`\
   → new short-lived system prompt set in context and sent to LLM
3. `inbound`:`LlmResponseCompletedEvent`\
   → LLM response received
4. `internal`:`AssistantReplyAcceptedEvent`\
   → assistant reply validated and attached to `activeTurn`\
   → request for audio synthesis and playback *(if TTS enabled)*
5. `inbound`:`TtsPlaybackStartedEvent` *(if TTS enabled)*\
   → audio playback has started
6. `internal`:`AssistantSpeechStartedEvent` *(if TTS enabled)*\
   → assistant enters speaking state
7. `inbound`:`TtsPlaybackCompletedEvent` *(if TTS enabled)*\
   → audio playback has finished
8. `internal`:`AssistantSpeechCompletedEvent` *(if TTS enabled)*\
   → assistant finished speaking
9. `internal`:`ConversationTurnCompletedEvent`\
   → turn finalized and not persisted in history

---

### Notes

- The LLM doesn't need the context history to ask for a clarification of the user input
- LLM generating/TTS speaking barges-in are still possible with unintelligible user input, the only difference is that
  the clarification turn isn't persisted in context
- Once the clarification turn is completed, the normal flow resumes with the conversation context from before the
  `SttTranscriptUnintelligbleEvent`

## State Transitions Summary

| Phase              | Event                | State Change          |
|--------------------|----------------------|-----------------------|
| Input              | Accepted Utterance   | thinking → generating |
| Input              | Clarification Needed | thinking → generating |
| Thinking Done      | LLM Response         | thinking → idle       |
| Speaking requested | TTS Synthetize       | speaking → preparing  |
| Speaking Start     | TTS Start            | speaking → speaking   |
| Speaking End       | TTS Done             | speaking → silent     |
| Completion         | Turn Completed       |                       |

---

## Key Design Principles

- Events represent facts, not intentions
- One event = one responsibility
- Internal events structure the flow
- Inbound events reflect real-world changes
- Orchestrator owns all decisions
