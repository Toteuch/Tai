package com.toteuch.tai.taiorchestrator.core;

import com.toteuch.tai.taiorchestrator.services.llm.LlmClient;
import com.toteuch.tai.taiorchestrator.services.llm.LlmGenerationResult;
import com.toteuch.tai.taiorchestrator.services.llm.LlmMessage;
import com.toteuch.tai.taiorchestrator.services.tts.TtsClient;
import com.toteuch.tai.taiorchestrator.services.ui.UiClient;
import com.toteuch.tai.taiorchestrator.session.ConversationTurn;
import com.toteuch.tai.taiorchestrator.session.InMemorySessionStore;
import com.toteuch.tai.taiorchestrator.session.SessionContext;
import com.toteuch.tai.taiorchestrator.session.SessionStore;
import com.toteuch.tai.taiorchestrator.session.TurnExecution;
import com.toteuch.tai.taiorchestrator.session.TurnExecutionStatus;
import com.toteuch.tai.taiorchestrator.state.InMemoryStateStore;
import com.toteuch.tai.taiorchestrator.state.ListeningState;
import com.toteuch.tai.taiorchestrator.state.SpeakingState;
import com.toteuch.tai.taiorchestrator.state.StateStore;
import com.toteuch.tai.taiorchestrator.state.ThinkingState;
import com.toteuch.tai.taiorchestrator.support.ContextAssembler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DefaultUserInputProcessorTest {

    private StateStore stateStore;
    private SessionStore sessionStore;
    private UiClient uiClient;
    private ContextAssembler contextAssembler;
    private LlmClient llmClient;
    private TtsClient ttsClient;

    private DefaultUserInputProcessor processor;

    @BeforeEach
    void setUp() {
        stateStore = new InMemoryStateStore();
        sessionStore = new InMemorySessionStore();
        uiClient = mock(UiClient.class);
        contextAssembler = new ContextAssembler();
        llmClient = mock(LlmClient.class);
        ttsClient = mock(TtsClient.class);

        processor = new DefaultUserInputProcessor(
            stateStore,
            sessionStore,
            uiClient,
            contextAssembler,
            llmClient,
            ttsClient
        );
    }

    @Test
    void shouldProcessUserTextAndTriggerTtsOnSuccessfulLlmResponse() {
        when(llmClient.generateReply(anyString(), anyString(), anyList()))
            .thenReturn(success("Hello Toteuch. I am Tai."));

        processor.processUserText("session-1", "corr-1", "Hello", false);

        SessionContext sessionContext = sessionStore.getOrCreate("session-1");
        ConversationTurn activeTurn = sessionContext.getActiveTurn();

        assertNotNull(activeTurn);
        assertEquals("Hello", activeTurn.getUserMessage());
        assertEquals("Hello Toteuch. I am Tai.", activeTurn.getAssistantMessage());
        assertTrue(activeTurn.isAssistantReplyGenerated());

        assertEquals(ListeningState.PROCESSING, stateStore.get().getListeningState());
        assertEquals(ThinkingState.IDLE, stateStore.get().getThinkingState());
        assertEquals(SpeakingState.PREPARING, stateStore.get().getSpeakingState());

        verify(uiClient).updateUserTranscript("session-1", "Hello");
        verify(uiClient).updateAssistantReply("session-1", "Hello Toteuch. I am Tai.");
        verify(ttsClient).speak("session-1", "corr-1", "Hello Toteuch. I am Tai.");

        assertNull(sessionContext.getCurrentExecution());
    }

    @Test
    void shouldNotTriggerTtsWhenTtsIsDisabled() {
        stateStore.get().setTtsEnabled(false);

        when(llmClient.generateReply(anyString(), anyString(), anyList()))
            .thenReturn(success("Text-only reply."));

        processor.processUserText("session-1", "corr-1", "Hello", false);

        verify(uiClient).updateAssistantReply("session-1", "Text-only reply.");
        verify(ttsClient, never()).speak(anyString(), anyString(), anyString());

        assertEquals(SpeakingState.SILENT, stateStore.get().getSpeakingState());
        assertEquals(ListeningState.IDLE, stateStore.get().getListeningState());
        assertEquals(ThinkingState.IDLE, stateStore.get().getThinkingState());
    }

    @Test
    void shouldHandleLlmFailure() {
        when(llmClient.generateReply(anyString(), anyString(), anyList()))
            .thenReturn(failure("LLM_ERROR", "Generation failed"));

        processor.processUserText("session-1", "corr-1", "Hello", false);

        verify(uiClient).showError("session-1", "Generation failed");
        verify(ttsClient, never()).speak(anyString(), anyString(), anyString());

        assertEquals(ThinkingState.IDLE, stateStore.get().getThinkingState());
        assertNull(sessionStore.getOrCreate("session-1").getCurrentExecution());
    }

    @Test
    void shouldSupersedePreviousExecutionAndIgnoreStaleLlmResult() {
        SessionContext sessionContext = sessionStore.getOrCreate("session-1");

        when(llmClient.generateReply(eq("session-1"), eq("corr-old"), anyList()))
            .thenAnswer(invocation -> {
                sessionContext.setCurrentExecution(new TurnExecution(
                    UUID.randomUUID().toString(),
                    "corr-new",
                    "corr-new",
                    "Stop",
                    Instant.now(),
                    TurnExecutionStatus.ACTIVE
                ));
                return success("Old reply that must be ignored.");
            });

        processor.processUserText("session-1", "corr-old", "Hello", false);

        verify(uiClient, never()).updateAssistantReply("session-1", "Old reply that must be ignored.");
        verify(ttsClient, never()).speak("session-1", "corr-old", "Old reply that must be ignored.");

        ConversationTurn activeTurn = sessionContext.getActiveTurn();
        assertNotNull(activeTurn);
        assertEquals("Hello", activeTurn.getUserMessage());
        assertFalse(activeTurn.isAssistantReplyGenerated());
    }

    @Test
    void shouldMarkPreviousTurnAsSupersededBeforeReplyWhenInterruptedDuringGeneration() {
        SessionContext sessionContext = sessionStore.getOrCreate("session-1");

        ConversationTurn previousTurn = new ConversationTurn("corr-old", "Hello", Instant.now());
        sessionContext.addTurn(previousTurn);
        sessionContext.setActiveTurn(previousTurn);
        sessionContext.setCurrentExecution(new TurnExecution(
            UUID.randomUUID().toString(),
            "corr-old",
            "corr-old",
            "Hello",
            Instant.now(),
            TurnExecutionStatus.ACTIVE
        ));

        when(llmClient.generateReply(anyString(), anyString(), anyList()))
            .thenReturn(success("Reply to new message"));

        processor.processUserText("session-1", "corr-new", "Stop", true);

        assertTrue(previousTurn.isSupersededBeforeAssistantReply());
        assertFalse(previousTurn.isAssistantPlaybackInterrupted());

        ConversationTurn activeTurn = sessionContext.getActiveTurn();
        assertNotNull(activeTurn);
        assertEquals("corr-new", activeTurn.getCorrelationId());
        assertEquals("Stop", activeTurn.getUserMessage());
    }

    @Test
    void shouldInterruptSpeakingTurnWhenNewInputArrives() {
        SessionContext sessionContext = sessionStore.getOrCreate("session-1");

        ConversationTurn previousTurn = new ConversationTurn("corr-old", "Tell me something", Instant.now());
        previousTurn.setAssistantMessage("I was speaking.");
        previousTurn.setAssistantReplyGenerated(true);
        previousTurn.setAssistantPlaybackStarted(true);

        sessionContext.addTurn(previousTurn);
        sessionContext.setActiveTurn(previousTurn);
        sessionContext.setCurrentExecution(new TurnExecution(
            UUID.randomUUID().toString(),
            "corr-old",
            "corr-old",
            "Tell me something",
            Instant.now(),
            TurnExecutionStatus.ACTIVE
        ));

        stateStore.get().setSpeakingState(SpeakingState.SPEAKING);

        when(llmClient.generateReply(anyString(), anyString(), anyList()))
            .thenReturn(success("New answer"));

        processor.processUserText("session-1", "corr-new", "Stop", true);

        verify(ttsClient).stop("session-1");
        assertTrue(previousTurn.isAssistantPlaybackInterrupted());
    }

    @Test
    void shouldBuildContextIncludingRecentTurns() {
        when(llmClient.generateReply(anyString(), anyString(), anyList()))
            .thenReturn(success("Second answer"));

        SessionContext sessionContext = sessionStore.getOrCreate("session-1");
        ConversationTurn turn = new ConversationTurn("corr-old", "First question", Instant.now());
        turn.setAssistantMessage("First answer");
        turn.setAssistantReplyGenerated(true);
        turn.setAssistantPlaybackStarted(true);
        turn.setAssistantPlaybackCompleted(true);
        sessionContext.addTurn(turn);

        processor.processUserText("session-1", "corr-new", "Second question", false);

        ArgumentCaptor<List<LlmMessage>> captor = ArgumentCaptor.forClass(List.class);
        verify(llmClient).generateReply(eq("session-1"), eq("corr-new"), captor.capture());

        assertFalse(captor.getValue().isEmpty());
    }

    private LlmGenerationResult success(String text) {
        return new LlmGenerationResult(
            true,
            text,
            "test-model",
            10,
            5,
            100L,
            null,
            null,
            false
        );
    }

    private LlmGenerationResult failure(String code, String message) {
        return new LlmGenerationResult(
            false,
            null,
            "test-model",
            null,
            null,
            100L,
            code,
            message,
            false
        );
    }
}
