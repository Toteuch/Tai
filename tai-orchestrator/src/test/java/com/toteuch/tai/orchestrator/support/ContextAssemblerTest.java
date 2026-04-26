package com.toteuch.tai.orchestrator.support;

import com.toteuch.tai.orchestrator.services.llm.LlmMessage;
import com.toteuch.tai.orchestrator.session.ConversationTurn;
import com.toteuch.tai.orchestrator.session.SessionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ContextAssemblerTest {

    private SystemPromptLoader systemPromptLoader;
    private ContextAssembler contextAssembler;

    @BeforeEach
    void setUp() {
        systemPromptLoader = mock(SystemPromptLoader.class);
        when(systemPromptLoader.getSystemPrompt()).thenReturn("You are Tai.");
        contextAssembler = new ContextAssembler(systemPromptLoader);
    }

    @Test
    void shouldNotDuplicateCurrentUserPromptWhenActiveTurnAlreadyExistsInSession() {
        SessionContext sessionContext = new SessionContext();

        ConversationTurn previousTurn = new ConversationTurn("corr-old", "Hello Tai", Instant.now(), true);
        previousTurn.setAssistantMessage("Hey.");
        previousTurn.setAssistantReplyGenerated(true);
        previousTurn.setAssistantPlaybackStarted(true);
        previousTurn.setAssistantPlaybackCompleted(true);
        sessionContext.addTurn(previousTurn);

        ConversationTurn activeTurn = new ConversationTurn("corr-current", "Explain RAM briefly", Instant.now(), true);
        sessionContext.addTurn(activeTurn);
        sessionContext.setActiveTurn(activeTurn);


        List<LlmMessage> messages = contextAssembler.assemble(
            sessionContext,
            "Explain RAM briefly",
            false
        );

        long currentPromptCount = messages.stream()
            .filter(message -> "user".equals(message.role()))
            .filter(message -> "Explain RAM briefly".equals(message.content()))
            .count();

        assertEquals(1, currentPromptCount, "The current user prompt must appear exactly once in the assembled context.");
    }

    @Test
    void shouldKeepPreviousConversationHistoryWhileAddingCurrentPromptOnce() {
        SessionContext sessionContext = new SessionContext();

        ConversationTurn previousTurn = new ConversationTurn("corr-old", "What is RAM?", Instant.now(), true);
        previousTurn.setAssistantMessage("RAM is short-term memory.");
        previousTurn.setAssistantReplyGenerated(true);
        previousTurn.setAssistantPlaybackStarted(true);
        previousTurn.setAssistantPlaybackCompleted(true);
        sessionContext.addTurn(previousTurn);

        ConversationTurn activeTurn = new ConversationTurn("corr-current", "Explain RAM briefly", Instant.now(), true);
        sessionContext.addTurn(activeTurn);
        sessionContext.setActiveTurn(activeTurn);

        List<LlmMessage> messages = contextAssembler.assemble(
            sessionContext,
            "Explain RAM briefly",
            false
        );

        assertTrue(messages.stream().anyMatch(m ->
            "user".equals(m.role()) && "What is RAM?".equals(m.content())
        ));

        assertTrue(messages.stream().anyMatch(m ->
            "assistant".equals(m.role()) && "RAM is short-term memory.".equals(m.content())
        ));

        long currentPromptCount = messages.stream()
            .filter(message -> "user".equals(message.role()))
            .filter(message -> "Explain RAM briefly".equals(message.content()))
            .count();

        assertEquals(1, currentPromptCount);
    }

    @Test
    void shouldIncludeInterruptionSystemNoteForSupersededTurn() {
        SessionContext sessionContext = new SessionContext();

        ConversationTurn supersededTurn = new ConversationTurn("corr-old", "Hello", Instant.now(), true);
        supersededTurn.setSupersededBeforeAssistantReply(true);
        sessionContext.addTurn(supersededTurn);

        ConversationTurn activeTurn = new ConversationTurn("corr-current", "Stop", Instant.now(), true);
        sessionContext.addTurn(activeTurn);
        sessionContext.setActiveTurn(activeTurn);

        List<LlmMessage> messages = contextAssembler.assemble(sessionContext, "Stop", false);

        assertTrue(messages.stream().anyMatch(m ->
            "system".equals(m.role())
                && m.content().contains("superseded by a newer one before you answered")
        ));
    }

    @Test
    void shouldIncludePlaybackInterruptedSystemNoteForInterruptedAssistantReply() {
        SessionContext sessionContext = new SessionContext();

        ConversationTurn interruptedTurn = new ConversationTurn("corr-old", "Tell me something", Instant.now(), true);
        interruptedTurn.setAssistantMessage("Here is something interesting.");
        interruptedTurn.setAssistantReplyGenerated(true);
        interruptedTurn.setAssistantPlaybackStarted(true);
        interruptedTurn.setAssistantPlaybackInterrupted(true);
        sessionContext.addTurn(interruptedTurn);

        ConversationTurn activeTurn = new ConversationTurn("corr-current", "No, wait", Instant.now(), true);
        sessionContext.addTurn(activeTurn);
        sessionContext.setActiveTurn(activeTurn);

        List<LlmMessage> messages = contextAssembler.assemble(sessionContext, "No, wait", false);

        assertTrue(messages.stream().anyMatch(m ->
            "system".equals(m.role())
                && m.content().contains("interrupted by the user before playback completed")
        ));
    }
}
