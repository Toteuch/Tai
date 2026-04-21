package com.toteuch.tai.taiorchestrator.core;

import com.toteuch.tai.taiorchestrator.services.llm.LlmClient;
import com.toteuch.tai.taiorchestrator.services.llm.LlmGenerationResult;
import com.toteuch.tai.taiorchestrator.services.llm.LlmMessage;
import com.toteuch.tai.taiorchestrator.services.tts.TtsClient;
import com.toteuch.tai.taiorchestrator.services.ui.UiClient;
import com.toteuch.tai.taiorchestrator.session.ConversationTurn;
import com.toteuch.tai.taiorchestrator.session.SessionContext;
import com.toteuch.tai.taiorchestrator.session.SessionStore;
import com.toteuch.tai.taiorchestrator.session.TurnExecution;
import com.toteuch.tai.taiorchestrator.session.TurnExecutionStatus;
import com.toteuch.tai.taiorchestrator.state.AssistantState;
import com.toteuch.tai.taiorchestrator.state.ListeningState;
import com.toteuch.tai.taiorchestrator.state.SpeakingState;
import com.toteuch.tai.taiorchestrator.state.StateStore;
import com.toteuch.tai.taiorchestrator.state.ThinkingState;
import com.toteuch.tai.taiorchestrator.support.ContextAssembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class DefaultUserInputProcessor implements UserInputProcessor {

    private static final Logger log = LoggerFactory.getLogger(DefaultUserInputProcessor.class);

    private final StateStore stateStore;
    private final SessionStore sessionStore;
    private final UiClient uiClient;
    private final ContextAssembler contextAssembler;
    private final LlmClient llmClient;
    private final TtsClient ttsClient;

    public DefaultUserInputProcessor(
        StateStore stateStore,
        SessionStore sessionStore,
        UiClient uiClient,
        ContextAssembler contextAssembler,
        LlmClient llmClient,
        TtsClient ttsClient
    ) {
        this.stateStore = stateStore;
        this.sessionStore = sessionStore;
        this.uiClient = uiClient;
        this.contextAssembler = contextAssembler;
        this.llmClient = llmClient;
        this.ttsClient = ttsClient;
    }

    @Override
    public void processUserText(
        String sessionId,
        String correlationId,
        String userText,
        boolean interruption
    ) {
        AssistantState state = stateStore.get();
        SessionContext sessionContext = sessionStore.getOrCreate(sessionId);

        markPreviousTurnAsInterruptedIfNeeded(sessionContext, correlationId);
        supersedePreviousExecutionIfNeeded(sessionContext, correlationId);

        if (state.getSpeakingState() == SpeakingState.SPEAKING || state.getSpeakingState() == SpeakingState.PREPARING) {
            ttsClient.stop(sessionId);
            state.setSpeakingState(SpeakingState.SILENT);
        }

        ConversationTurn newTurn = new ConversationTurn(correlationId, userText, Instant.now());
        sessionContext.addTurn(newTurn);
        sessionContext.setActiveTurn(newTurn);

        TurnExecution currentExecution = new TurnExecution(
            UUID.randomUUID().toString(),
            correlationId,
            correlationId,
            userText,
            Instant.now(),
            TurnExecutionStatus.ACTIVE
        );
        sessionContext.setCurrentExecution(currentExecution);

        state.setCurrentSessionId(sessionId);
        state.setCurrentUserText(userText);
        state.setListeningState(ListeningState.PROCESSING);
        state.setThinkingState(ThinkingState.GENERATING);

        uiClient.updateUserTranscript(sessionId, userText);
        uiClient.updateAssistantState(sessionId, state);

        List<LlmMessage> messages = contextAssembler.assemble(sessionContext, state, userText);
        LlmGenerationResult result = llmClient.generateReply(sessionId, correlationId, messages);

        if (!isStillActiveExecution(sessionContext, correlationId)) {
            log.info("Ignoring stale LLM result | sessionId={} correlationId={}", sessionId, correlationId);
            return;
        }

        ConversationTurn activeTurn = sessionContext.getActiveTurn();
        if (activeTurn == null || !correlationId.equals(activeTurn.getCorrelationId())) {
            log.info("Ignoring LLM result because active turn changed | sessionId={} correlationId={}", sessionId, correlationId);
            return;
        }

        if (result.success()) {
            applySuccessfulLlmResult(sessionId, correlationId, state, sessionContext, activeTurn, result);
        } else {
            applyFailedLlmResult(sessionId, correlationId, state, sessionContext, result);
        }
    }

    private void applySuccessfulLlmResult(
        String sessionId,
        String correlationId,
        AssistantState state,
        SessionContext sessionContext,
        ConversationTurn activeTurn,
        LlmGenerationResult result
    ) {
        state.setThinkingState(ThinkingState.IDLE);
        state.setCurrentAssistantText(result.responseText());

        activeTurn.setAssistantMessage(result.responseText());
        activeTurn.setAssistantReplyGenerated(true);

        TurnExecution currentExecution = sessionContext.getCurrentExecution();
        if (currentExecution != null && correlationId.equals(currentExecution.getCorrelationId())) {
            currentExecution.setStatus(TurnExecutionStatus.COMPLETED);
            sessionContext.setCurrentExecution(null);
        }

        uiClient.updateAssistantReply(sessionId, result.responseText());

        if (state.isTtsEnabled()) {
            state.setSpeakingState(SpeakingState.PREPARING);
            uiClient.updateAssistantState(sessionId, state);
            ttsClient.speak(sessionId, correlationId, result.responseText());
        } else {
            state.setSpeakingState(SpeakingState.SILENT);
            state.setListeningState(ListeningState.IDLE);
            uiClient.updateAssistantState(sessionId, state);
        }
    }

    private void applyFailedLlmResult(
        String sessionId,
        String correlationId,
        AssistantState state,
        SessionContext sessionContext,
        LlmGenerationResult result
    ) {
        state.setThinkingState(ThinkingState.IDLE);

        TurnExecution currentExecution = sessionContext.getCurrentExecution();
        if (currentExecution != null && correlationId.equals(currentExecution.getCorrelationId())) {
            currentExecution.setStatus(TurnExecutionStatus.FAILED);
            sessionContext.setCurrentExecution(null);
        }

        uiClient.showError(sessionId, result.errorMessage());
        uiClient.updateAssistantState(sessionId, state);
    }

    private void supersedePreviousExecutionIfNeeded(SessionContext sessionContext, String newCorrelationId) {
        TurnExecution currentExecution = sessionContext.getCurrentExecution();
        if (currentExecution == null) {
            return;
        }

        if (!currentExecution.isActive()) {
            return;
        }

        if (newCorrelationId.equals(currentExecution.getCorrelationId())) {
            return;
        }

        currentExecution.setStatus(TurnExecutionStatus.SUPERSEDED);
        currentExecution.setSupersededByCorrelationId(newCorrelationId);
    }

    private void markPreviousTurnAsInterruptedIfNeeded(SessionContext sessionContext, String newCorrelationId) {
        ConversationTurn activeTurn = sessionContext.getActiveTurn();
        if (activeTurn == null) {
            return;
        }

        if (newCorrelationId.equals(activeTurn.getCorrelationId())) {
            return;
        }

        boolean noAssistantReplyYet = !activeTurn.isAssistantReplyGenerated();
        boolean assistantWasSpeaking =
            activeTurn.isAssistantPlaybackStarted() && !activeTurn.isAssistantPlaybackCompleted();

        if (noAssistantReplyYet) {
            activeTurn.setSupersededBeforeAssistantReply(true);
        }

        if (assistantWasSpeaking) {
            activeTurn.setAssistantPlaybackInterrupted(true);
        }
    }

    private boolean isStillActiveExecution(SessionContext sessionContext, String correlationId) {
        TurnExecution currentExecution = sessionContext.getCurrentExecution();
        return currentExecution != null
            && currentExecution.isActive()
            && correlationId.equals(currentExecution.getCorrelationId());
    }
}
