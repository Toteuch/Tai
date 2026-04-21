package com.toteuch.tai.taiorchestrator.session;

import java.time.Instant;

/**
 * Represents a single conversational turn within a session.
 *
 * <p>A conversation turn is the persistent conversational memory unit used by the
 * orchestrator. It stores what the user said, what Tai replied, and how that reply
 * progressed through the runtime lifecycle.</p>
 *
 * <p>This class is intentionally richer than a simple user/assistant message pair.
 * It captures whether an assistant reply was generated, whether playback started,
 * whether playback completed, and whether the turn was interrupted or superseded.</p>
 *
 * <p>This distinction is important because the runtime execution of a turn and the
 * conversational memory of a turn are not the same thing. A turn may exist in the
 * conversation history even if its execution was interrupted or replaced by a newer one.</p>
 *
 * <p>Typical examples:
 * <ul>
 *     <li>A user message is received, but a newer one replaces it before Tai answers.</li>
 *     <li>Tai generates a reply, starts speaking, and is interrupted by the user.</li>
 *     <li>A full user/assistant exchange completes normally.</li>
 * </ul>
 * </p>
 */
public class ConversationTurn {

    private final String correlationId;
    private final Instant createdAt;

    private String userMessage;
    private String assistantMessage;

    private boolean assistantReplyGenerated;
    private boolean assistantPlaybackStarted;
    private boolean assistantPlaybackCompleted;
    private boolean assistantPlaybackInterrupted;

    private boolean supersededBeforeAssistantReply;

    public ConversationTurn(String correlationId, String userMessage, Instant createdAt) {
        this.correlationId = correlationId;
        this.userMessage = userMessage;
        this.createdAt = createdAt;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getAssistantMessage() {
        return assistantMessage;
    }

    public void setAssistantMessage(String assistantMessage) {
        this.assistantMessage = assistantMessage;
    }

    public boolean isAssistantReplyGenerated() {
        return assistantReplyGenerated;
    }

    public void setAssistantReplyGenerated(boolean assistantReplyGenerated) {
        this.assistantReplyGenerated = assistantReplyGenerated;
    }

    public boolean isAssistantPlaybackStarted() {
        return assistantPlaybackStarted;
    }

    public void setAssistantPlaybackStarted(boolean assistantPlaybackStarted) {
        this.assistantPlaybackStarted = assistantPlaybackStarted;
    }

    public boolean isAssistantPlaybackCompleted() {
        return assistantPlaybackCompleted;
    }

    public void setAssistantPlaybackCompleted(boolean assistantPlaybackCompleted) {
        this.assistantPlaybackCompleted = assistantPlaybackCompleted;
    }

    public boolean isAssistantPlaybackInterrupted() {
        return assistantPlaybackInterrupted;
    }

    public void setAssistantPlaybackInterrupted(boolean assistantPlaybackInterrupted) {
        this.assistantPlaybackInterrupted = assistantPlaybackInterrupted;
    }

    public boolean isSupersededBeforeAssistantReply() {
        return supersededBeforeAssistantReply;
    }

    public void setSupersededBeforeAssistantReply(boolean supersededBeforeAssistantReply) {
        this.supersededBeforeAssistantReply = supersededBeforeAssistantReply;
    }
}
