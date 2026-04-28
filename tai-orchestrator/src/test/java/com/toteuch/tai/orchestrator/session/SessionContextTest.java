// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.session;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class SessionContextTest {

    @Test
    void should_initialize_with_default_states() {
        SessionContext context = new SessionContext();

        assertThat(context.getThinkingState()).isEqualTo(ThinkingState.IDLE);
        assertThat(context.getSpeakingState()).isEqualTo(SpeakingState.SILENT);
        assertThat(context.getTurns()).isEmpty();
        assertThat(context.getActiveTurn()).isNull();
        assertThat(context.isTtsEnabled()).isTrue();
    }

    @Test
    void should_add_turn_and_store_it() {
        SessionContext context = new SessionContext();
        ConversationTurn turn = new ConversationTurn("corr-1", "Hello", Instant.now(), true);

        context.addTurn(turn);

        assertThat(context.getTurns()).containsExactly(turn);
    }

    @Test
    void should_detect_active_turn_correctly() {
        SessionContext context = new SessionContext();
        ConversationTurn turn = new ConversationTurn("corr-1", "Hello", Instant.now(), true);
        context.setActiveTurn(turn);

        assertThat(context.isStillActiveTurn("corr-1")).isTrue();
        assertThat(context.isStillActiveTurn("corr-2")).isFalse();
    }

    @Test
    void should_handle_null_active_turn_in_isStillActiveTurn() {
        SessionContext context = new SessionContext();

        assertThat(context.isStillActiveTurn("any")).isFalse();
    }
}
