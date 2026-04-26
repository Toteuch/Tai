package com.toteuch.tai.orchestrator.session;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    void should_barge_in_during_speaking() {
        SessionContext context = new SessionContext();

        ConversationTurn turn = new ConversationTurn("corr-1", "Hello", Instant.now(), true);
        context.setActiveTurn(turn);
        context.setSpeakingState(SpeakingState.SPEAKING);

        boolean result = context.bargeIn("corr-2");

        assertThat(result).isTrue();
        assertThat(context.getSpeakingState()).isEqualTo(SpeakingState.SILENT);
        assertThat(turn.isAssistantPlaybackInterrupted()).isTrue();
    }

    @Test
    void should_barge_in_during_preparing() {
        SessionContext context = new SessionContext();

        ConversationTurn turn = new ConversationTurn("corr-1", "Hello", Instant.now(), true);
        context.setActiveTurn(turn);
        context.setSpeakingState(SpeakingState.PREPARING);

        boolean result = context.bargeIn("corr-2");

        assertThat(result).isTrue();
        assertThat(context.getSpeakingState()).isEqualTo(SpeakingState.SILENT);
        assertThat(turn.isAssistantPlaybackInterrupted()).isTrue();
    }

    @Test
    void should_barge_in_during_thinking() {
        SessionContext context = new SessionContext();

        ConversationTurn turn = new ConversationTurn("corr-1", "Hello", Instant.now(), true);
        context.setActiveTurn(turn);
        context.setThinkingState(ThinkingState.GENERATING);

        boolean result = context.bargeIn("corr-2");

        assertThat(result).isTrue();
        assertThat(context.getThinkingState()).isEqualTo(ThinkingState.IDLE);
        assertThat(turn.isSupersededBeforeAssistantReply()).isTrue();
    }

    @Test
    void should_not_barge_in_if_same_correlation_id() {
        SessionContext context = new SessionContext();

        ConversationTurn turn = new ConversationTurn("corr-1", "Hello", Instant.now(), true);
        context.setActiveTurn(turn);

        boolean result = context.bargeIn("corr-1");

        assertThat(result).isFalse();
    }

    @Test
    void should_not_barge_in_if_no_active_turn() {
        SessionContext context = new SessionContext();

        boolean result = context.bargeIn("corr-1");

        assertThat(result).isFalse();
    }

    @Test
    void should_not_interrupt_if_tts_disabled() {
        SessionContext context = new SessionContext();
        context.setTtsEnabled(false);

        ConversationTurn turn = new ConversationTurn("corr-1", "Hello", Instant.now(), true);
        context.setActiveTurn(turn);
        context.setSpeakingState(SpeakingState.SPEAKING);

        boolean result = context.bargeIn("corr-2");

        assertThat(result).isTrue();
        assertThat(turn.isAssistantPlaybackInterrupted()).isFalse();
    }
}
