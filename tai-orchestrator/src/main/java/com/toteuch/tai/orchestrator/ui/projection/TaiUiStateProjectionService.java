// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.projection;

import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SessionStore;
import com.toteuch.tai.orchestrator.ui.model.TaiUiState;
import java.time.Clock;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class TaiUiStateProjectionService {

    private static final String SCHEMA_VERSION = "2.0";

    private final SessionStore sessionStore;
    private final ConversationStatusProjector conversationStatusProjector;
    private final ModuleOverviewProjector moduleOverviewProjector;
    private final UtteranceProjector utteranceProjector;
    private final TaiUiStateStore store;
    private final Clock clock;
    private final AtomicLong sequence = new AtomicLong();

    public TaiUiStateProjectionService(
            SessionStore sessionStore,
            ConversationStatusProjector conversationStatusProjector,
            ModuleOverviewProjector moduleOverviewProjector,
            UtteranceProjector utteranceProjector,
            TaiUiStateStore store,
            Clock clock) {
        this.sessionStore = sessionStore;
        this.conversationStatusProjector = conversationStatusProjector;
        this.moduleOverviewProjector = moduleOverviewProjector;
        this.utteranceProjector = utteranceProjector;
        this.store = store;
        this.clock = clock;
    }

    public TaiUiState currentOrRebuild() {
        return store.current().orElseGet(this::rebuild);
    }

    public TaiUiState rebuild() {
        SessionContext sessionContext = sessionStore.get();

        TaiUiState state =
                new TaiUiState(
                        SCHEMA_VERSION,
                        sequence.incrementAndGet(),
                        clock.instant(),
                        conversationStatusProjector.project(),
                        moduleOverviewProjector.project(),
                        utteranceProjector.projectLastUserUtterance(sessionContext),
                        utteranceProjector.projectLastAssistantUtterance(sessionContext));

        return store.update(state);
    }
}
