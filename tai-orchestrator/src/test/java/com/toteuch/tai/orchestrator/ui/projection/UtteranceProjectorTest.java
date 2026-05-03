// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.projection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.toteuch.tai.orchestrator.session.ConversationTurn;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.TurnOutcome;
import com.toteuch.tai.orchestrator.ui.model.ModuleHealth;
import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import com.toteuch.tai.orchestrator.ui.model.Utterance;
import com.toteuch.tai.orchestrator.ui.model.UtteranceRole;
import com.toteuch.tai.orchestrator.ui.model.UtteranceStatus;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleActivity;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeRegistry;
import com.toteuch.tai.orchestrator.ui.runtime.ModuleRuntimeSnapshot;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class UtteranceProjectorTest {

    private static final Instant NOW = Instant.parse("2026-05-01T10:00:00Z");
    private static final Instant STARTED_AT = Instant.parse("2026-05-01T09:59:50Z");
    private static final Instant COMPLETED_AT = Instant.parse("2026-05-01T09:59:58Z");

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private ModuleRuntimeRegistry registry;
    private UtteranceProjector projector;

    @BeforeEach
    void set_up() {
        registry = new ModuleRuntimeRegistry(clock);
        projector = new UtteranceProjector(registry);
    }

    @Test
    void project_last_user_utterance_should_use_active_turn_when_it_has_user_text() {
        ConversationTurn activeTurn =
                turn("active-correlation-id", "Active user text", null, STARTED_AT, null, null);

        SessionContext sessionContext =
                sessionContext(
                        activeTurn,
                        List.of(
                                turn(
                                        "history-correlation-id",
                                        "History user text",
                                        "History assistant text",
                                        STARTED_AT.minusSeconds(30),
                                        COMPLETED_AT.minusSeconds(30),
                                        TurnOutcome.COMPLETED)));

        Utterance utterance = projector.projectLastUserUtterance(sessionContext);

        assertThat(utterance.role()).isEqualTo(UtteranceRole.USER);
        assertThat(utterance.text()).isEqualTo("Active user text");
        assertThat(utterance.startedAt()).isEqualTo(STARTED_AT);
        assertThat(utterance.updatedAt()).isEqualTo(STARTED_AT);
        assertThat(utterance.correlationId()).isEqualTo("active-correlation-id");
        assertThat(utterance.status()).isEqualTo(UtteranceStatus.COMPLETED);
    }

    @Test
    void project_last_user_utterance_should_fallback_to_latest_history_turn_with_user_text() {
        ConversationTurn olderTurn =
                turn(
                        "older-correlation-id",
                        "Older user text",
                        "Older assistant text",
                        STARTED_AT.minusSeconds(60),
                        COMPLETED_AT.minusSeconds(60),
                        TurnOutcome.COMPLETED);

        ConversationTurn latestTurn =
                turn(
                        "latest-correlation-id",
                        "Latest user text",
                        "Latest assistant text",
                        STARTED_AT,
                        COMPLETED_AT,
                        TurnOutcome.COMPLETED);

        SessionContext sessionContext = sessionContext(null, List.of(olderTurn, latestTurn));

        Utterance utterance = projector.projectLastUserUtterance(sessionContext);

        assertThat(utterance.role()).isEqualTo(UtteranceRole.USER);
        assertThat(utterance.text()).isEqualTo("Latest user text");
        assertThat(utterance.startedAt()).isEqualTo(STARTED_AT);
        assertThat(utterance.updatedAt()).isEqualTo(COMPLETED_AT);
        assertThat(utterance.correlationId()).isEqualTo("latest-correlation-id");
        assertThat(utterance.status()).isEqualTo(UtteranceStatus.COMPLETED);
    }

    @Test
    void project_last_user_utterance_should_return_null_when_no_user_text_exists() {
        SessionContext sessionContext =
                sessionContext(
                        null,
                        List.of(
                                turn(
                                        "assistant-only-correlation-id",
                                        null,
                                        "Assistant text",
                                        STARTED_AT,
                                        COMPLETED_AT,
                                        TurnOutcome.COMPLETED)));

        Utterance utterance = projector.projectLastUserUtterance(sessionContext);

        assertThat(utterance).isNull();
    }

    @Test
    void project_last_assistant_utterance_should_use_active_turn_when_it_has_assistant_text() {
        updateTts(ModuleHealth.UP, ModuleActivity.SPEAKING);

        ConversationTurn activeTurn =
                turn(
                        "active-correlation-id",
                        "Active user text",
                        "Active assistant text",
                        STARTED_AT,
                        null,
                        null);

        SessionContext sessionContext =
                sessionContext(
                        activeTurn,
                        List.of(
                                turn(
                                        "history-correlation-id",
                                        "History user text",
                                        "History assistant text",
                                        STARTED_AT.minusSeconds(30),
                                        COMPLETED_AT.minusSeconds(30),
                                        TurnOutcome.COMPLETED)));

        Utterance utterance = projector.projectLastAssistantUtterance(sessionContext);

        assertThat(utterance.role()).isEqualTo(UtteranceRole.ASSISTANT);
        assertThat(utterance.text()).isEqualTo("Active assistant text");
        assertThat(utterance.startedAt()).isEqualTo(STARTED_AT);
        assertThat(utterance.updatedAt()).isEqualTo(STARTED_AT);
        assertThat(utterance.correlationId()).isEqualTo("active-correlation-id");
        assertThat(utterance.status()).isEqualTo(UtteranceStatus.STARTED);
    }

    @Test
    void
            project_last_assistant_utterance_should_fallback_to_latest_history_turn_with_assistant_text() {
        ConversationTurn olderTurn =
                turn(
                        "older-correlation-id",
                        "Older user text",
                        "Older assistant text",
                        STARTED_AT.minusSeconds(60),
                        COMPLETED_AT.minusSeconds(60),
                        TurnOutcome.COMPLETED);

        ConversationTurn latestTurn =
                turn(
                        "latest-correlation-id",
                        "Latest user text",
                        "Latest assistant text",
                        STARTED_AT,
                        COMPLETED_AT,
                        TurnOutcome.COMPLETED);

        SessionContext sessionContext = sessionContext(null, List.of(olderTurn, latestTurn));

        Utterance utterance = projector.projectLastAssistantUtterance(sessionContext);

        assertThat(utterance.role()).isEqualTo(UtteranceRole.ASSISTANT);
        assertThat(utterance.text()).isEqualTo("Latest assistant text");
        assertThat(utterance.startedAt()).isEqualTo(STARTED_AT);
        assertThat(utterance.updatedAt()).isEqualTo(COMPLETED_AT);
        assertThat(utterance.correlationId()).isEqualTo("latest-correlation-id");
        assertThat(utterance.status()).isEqualTo(UtteranceStatus.COMPLETED);
    }

    @Test
    void project_last_assistant_utterance_should_return_started_when_active_turn_is_synthesizing() {
        updateTts(ModuleHealth.UP, ModuleActivity.SYNTHESIZING);

        ConversationTurn activeTurn =
                turn(
                        "active-correlation-id",
                        "User text",
                        "Assistant text",
                        STARTED_AT,
                        null,
                        null);

        SessionContext sessionContext = sessionContext(activeTurn, List.of());

        Utterance utterance = projector.projectLastAssistantUtterance(sessionContext);

        assertThat(utterance.status()).isEqualTo(UtteranceStatus.STARTED);
    }

    @Test
    void project_last_assistant_utterance_should_return_started_when_active_turn_is_speaking() {
        updateTts(ModuleHealth.UP, ModuleActivity.SPEAKING);

        ConversationTurn activeTurn =
                turn(
                        "active-correlation-id",
                        "User text",
                        "Assistant text",
                        STARTED_AT,
                        null,
                        null);

        SessionContext sessionContext = sessionContext(activeTurn, List.of());

        Utterance utterance = projector.projectLastAssistantUtterance(sessionContext);

        assertThat(utterance.status()).isEqualTo(UtteranceStatus.STARTED);
    }

    @Test
    void project_last_assistant_utterance_should_return_failed_when_active_tts_is_error() {
        updateTts(ModuleHealth.DEGRADED, ModuleActivity.ERROR);

        ConversationTurn activeTurn =
                turn(
                        "active-correlation-id",
                        "User text",
                        "Assistant text",
                        STARTED_AT,
                        null,
                        null);

        SessionContext sessionContext = sessionContext(activeTurn, List.of());

        Utterance utterance = projector.projectLastAssistantUtterance(sessionContext);

        assertThat(utterance.status()).isEqualTo(UtteranceStatus.FAILED);
    }

    @Test
    void project_last_assistant_utterance_should_return_failed_for_failed_history_turn() {
        ConversationTurn failedTurn =
                turn(
                        "failed-correlation-id",
                        "User text",
                        "Assistant text",
                        STARTED_AT,
                        COMPLETED_AT,
                        TurnOutcome.FAILED);

        SessionContext sessionContext = sessionContext(null, List.of(failedTurn));

        Utterance utterance = projector.projectLastAssistantUtterance(sessionContext);

        assertThat(utterance.status()).isEqualTo(UtteranceStatus.FAILED);
    }

    @Test
    void project_last_assistant_utterance_should_return_interrupted_for_interrupted_history_turn() {
        ConversationTurn interruptedTurn =
                turn(
                        "interrupted-correlation-id",
                        "User text",
                        "Assistant text",
                        STARTED_AT,
                        COMPLETED_AT,
                        TurnOutcome.INTERRUPTED);

        SessionContext sessionContext = sessionContext(null, List.of(interruptedTurn));

        Utterance utterance = projector.projectLastAssistantUtterance(sessionContext);

        assertThat(utterance.status()).isEqualTo(UtteranceStatus.INTERRUPTED);
    }

    @Test
    void project_last_assistant_utterance_should_return_interrupted_for_superseded_history_turn() {
        ConversationTurn supersededTurn =
                turn(
                        "superseded-correlation-id",
                        "User text",
                        "Assistant text",
                        STARTED_AT,
                        COMPLETED_AT,
                        TurnOutcome.SUPERSEDED);

        SessionContext sessionContext = sessionContext(null, List.of(supersededTurn));

        Utterance utterance = projector.projectLastAssistantUtterance(sessionContext);

        assertThat(utterance.status()).isEqualTo(UtteranceStatus.INTERRUPTED);
    }

    @Test
    void project_last_assistant_utterance_should_return_completed_for_completed_history_turn() {
        ConversationTurn completedTurn =
                turn(
                        "completed-correlation-id",
                        "User text",
                        "Assistant text",
                        STARTED_AT,
                        COMPLETED_AT,
                        TurnOutcome.COMPLETED);

        SessionContext sessionContext = sessionContext(null, List.of(completedTurn));

        Utterance utterance = projector.projectLastAssistantUtterance(sessionContext);

        assertThat(utterance.status()).isEqualTo(UtteranceStatus.COMPLETED);
    }

    @Test
    void project_last_assistant_utterance_should_return_null_when_no_assistant_state_exists() {
        ConversationTurn userOnlyTurn =
                turn(
                        "user-only-correlation-id",
                        "User text",
                        null,
                        STARTED_AT,
                        COMPLETED_AT,
                        TurnOutcome.COMPLETED);

        SessionContext sessionContext = sessionContext(userOnlyTurn, List.of());

        Utterance utterance = projector.projectLastAssistantUtterance(sessionContext);

        assertThat(utterance).isNull();
    }

    private SessionContext sessionContext(
            ConversationTurn activeTurn, List<ConversationTurn> turns) {
        SessionContext sessionContext = Mockito.mock(SessionContext.class);

        when(sessionContext.getActiveTurn()).thenReturn(activeTurn);
        when(sessionContext.getTurns()).thenReturn(turns);

        return sessionContext;
    }

    private ConversationTurn turn(
            String correlationId,
            String userText,
            String assistantText,
            Instant startedAt,
            Instant completedAt,
            TurnOutcome outcome) {
        ConversationTurn turn = Mockito.mock(ConversationTurn.class);

        when(turn.getCorrelationId()).thenReturn(correlationId);
        when(turn.getUserMessage()).thenReturn(userText);
        when(turn.getAssistantMessage()).thenReturn(assistantText);
        when(turn.getCreatedAt()).thenReturn(startedAt);
        when(turn.getCompletedAt()).thenReturn(completedAt);
        when(turn.getOutcome()).thenReturn(outcome);

        return turn;
    }

    private void updateTts(ModuleHealth health, ModuleActivity activity) {
        registry.update(
                new ModuleRuntimeSnapshot(
                        TaiModule.TTS_PIPER,
                        health,
                        activity,
                        NOW,
                        NOW,
                        "correlation-id",
                        null,
                        Map.of()));
    }
}
