// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.projection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SessionStore;
import com.toteuch.tai.orchestrator.ui.model.ConversationStatus;
import com.toteuch.tai.orchestrator.ui.model.ModuleHealth;
import com.toteuch.tai.orchestrator.ui.model.ModuleOverview;
import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import com.toteuch.tai.orchestrator.ui.model.TaiUiState;
import com.toteuch.tai.orchestrator.ui.model.Utterance;
import com.toteuch.tai.orchestrator.ui.model.UtteranceRole;
import com.toteuch.tai.orchestrator.ui.model.UtteranceStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TaiUiStateProjectionServiceTest {

    private static final Instant NOW = Instant.parse("2026-05-01T10:00:00Z");

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private SessionStore sessionStore;
    private ConversationStatusProjector conversationStatusProjector;
    private ModuleOverviewProjector moduleOverviewProjector;
    private UtteranceProjector utteranceProjector;
    private TaiUiStateStore store;
    private TaiUiStateProjectionService service;

    @BeforeEach
    void set_up() {
        sessionStore = Mockito.mock(SessionStore.class);
        conversationStatusProjector = Mockito.mock(ConversationStatusProjector.class);
        moduleOverviewProjector = Mockito.mock(ModuleOverviewProjector.class);
        utteranceProjector = Mockito.mock(UtteranceProjector.class);
        store = new TaiUiStateStore();

        service =
                new TaiUiStateProjectionService(
                        sessionStore,
                        conversationStatusProjector,
                        moduleOverviewProjector,
                        utteranceProjector,
                        store,
                        clock);
    }

    @Test
    void
            current_or_rebuild_should_return_current_state_without_rebuilding_when_store_already_contains_state() {
        TaiUiState existingState = existingState();

        store.update(existingState);

        TaiUiState result = service.currentOrRebuild();

        assertThat(result).isSameAs(existingState);

        verifyNoInteractions(sessionStore);
        verifyNoInteractions(conversationStatusProjector);
        verifyNoInteractions(moduleOverviewProjector);
        verifyNoInteractions(utteranceProjector);
    }

    @Test
    void current_or_rebuild_should_rebuild_state_when_store_is_empty() {
        SessionContext sessionContext = Mockito.mock(SessionContext.class);
        Map<TaiModule, ModuleOverview> modules = modules();
        Utterance userUtterance = userUtterance("correlation-id");
        Utterance assistantUtterance = assistantUtterance("correlation-id");

        when(sessionStore.get()).thenReturn(sessionContext);
        when(conversationStatusProjector.project()).thenReturn(ConversationStatus.THINKING);
        when(moduleOverviewProjector.project()).thenReturn(modules);
        when(utteranceProjector.projectLastUserUtterance(sessionContext)).thenReturn(userUtterance);
        when(utteranceProjector.projectLastAssistantUtterance(sessionContext))
                .thenReturn(assistantUtterance);

        TaiUiState result = service.currentOrRebuild();

        assertThat(result.schemaVersion()).isEqualTo("2.0");
        assertThat(result.sequence()).isEqualTo(1L);
        assertThat(result.generatedAt()).isEqualTo(NOW);
        assertThat(result.conversationStatus()).isEqualTo(ConversationStatus.THINKING);
        assertThat(result.modules()).isEqualTo(modules);
        assertThat(result.lastUserUtterance()).isEqualTo(userUtterance);
        assertThat(result.lastAssistantUtterance()).isEqualTo(assistantUtterance);

        assertThat(store.current()).contains(result);

        verify(sessionStore).get();
        verify(conversationStatusProjector).project();
        verify(moduleOverviewProjector).project();
        verify(utteranceProjector).projectLastUserUtterance(sessionContext);
        verify(utteranceProjector).projectLastAssistantUtterance(sessionContext);
    }

    @Test
    void rebuild_should_always_recompute_and_replace_current_state() {
        SessionContext sessionContext = Mockito.mock(SessionContext.class);
        TaiUiState existingState = existingState();
        Map<TaiModule, ModuleOverview> modules = modules();
        Utterance userUtterance = userUtterance("new-correlation-id");
        Utterance assistantUtterance = assistantUtterance("new-correlation-id");

        store.update(existingState);

        when(sessionStore.get()).thenReturn(sessionContext);
        when(conversationStatusProjector.project()).thenReturn(ConversationStatus.SPEAKING);
        when(moduleOverviewProjector.project()).thenReturn(modules);
        when(utteranceProjector.projectLastUserUtterance(sessionContext)).thenReturn(userUtterance);
        when(utteranceProjector.projectLastAssistantUtterance(sessionContext))
                .thenReturn(assistantUtterance);

        TaiUiState result = service.rebuild();

        assertThat(result).isNotSameAs(existingState);
        assertThat(result.schemaVersion()).isEqualTo("2.0");
        assertThat(result.sequence()).isEqualTo(1L);
        assertThat(result.generatedAt()).isEqualTo(NOW);
        assertThat(result.conversationStatus()).isEqualTo(ConversationStatus.SPEAKING);
        assertThat(result.modules()).isEqualTo(modules);
        assertThat(result.lastUserUtterance()).isEqualTo(userUtterance);
        assertThat(result.lastAssistantUtterance()).isEqualTo(assistantUtterance);

        assertThat(store.current()).contains(result);
    }

    @Test
    void rebuild_should_increment_sequence_on_each_rebuild() {
        SessionContext sessionContext = Mockito.mock(SessionContext.class);

        when(sessionStore.get()).thenReturn(sessionContext);
        when(conversationStatusProjector.project()).thenReturn(ConversationStatus.IDLE);
        when(moduleOverviewProjector.project()).thenReturn(modules());
        when(utteranceProjector.projectLastUserUtterance(sessionContext)).thenReturn(null);
        when(utteranceProjector.projectLastAssistantUtterance(sessionContext)).thenReturn(null);

        TaiUiState first = service.rebuild();
        TaiUiState second = service.rebuild();
        TaiUiState third = service.rebuild();

        assertThat(first.sequence()).isEqualTo(1L);
        assertThat(second.sequence()).isEqualTo(2L);
        assertThat(third.sequence()).isEqualTo(3L);

        assertThat(store.current()).contains(third);
    }

    private TaiUiState existingState() {
        return new TaiUiState(
                "2.0",
                42L,
                Instant.parse("2026-05-01T09:00:00Z"),
                ConversationStatus.IDLE,
                modules(),
                userUtterance("existing-correlation-id"),
                assistantUtterance("existing-correlation-id"));
    }

    private Map<TaiModule, ModuleOverview> modules() {
        return Map.of(
                TaiModule.SYSTEM,
                new ModuleOverview(ModuleHealth.UP, "Healthy", NOW, false),
                TaiModule.LLM,
                new ModuleOverview(ModuleHealth.UP, "Generating", NOW, false));
    }

    private Utterance userUtterance(String correlationId) {
        return new Utterance(
                UtteranceRole.USER,
                "Hello Tai",
                Instant.parse("2026-05-01T09:59:50Z"),
                Instant.parse("2026-05-01T09:59:51Z"),
                correlationId,
                UtteranceStatus.COMPLETED);
    }

    private Utterance assistantUtterance(String correlationId) {
        return new Utterance(
                UtteranceRole.ASSISTANT,
                "Hello. How can I help?",
                Instant.parse("2026-05-01T09:59:52Z"),
                Instant.parse("2026-05-01T09:59:58Z"),
                correlationId,
                UtteranceStatus.COMPLETED);
    }
}
