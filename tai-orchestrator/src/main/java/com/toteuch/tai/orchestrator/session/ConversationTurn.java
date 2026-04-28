// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.session;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger contextLog = LoggerFactory.getLogger("tai.context");

    private final String correlationId;
    private final Instant createdAt;
    private final String userMessage;
    private final boolean persistInHistory;
    private String assistantMessage;
    private boolean assistantReplyGenerated;
    private boolean assistantPlaybackStarted;
    private boolean assistantPlaybackCompleted;
    private boolean supersededBeforeAssistantReply;
    private boolean assistantPlaybackInterrupted;
    private String supersededByCorrelationId;

    public ConversationTurn(
            String correlationId, String userMessage, Instant createdAt, boolean persistInHistory) {
        contextLog.info(
                "New conversation turn | correlationId={} persistInHistory={}",
                correlationId,
                persistInHistory);
        contextLog.debug("userMessage={}", userMessage);
        this.correlationId = correlationId;
        this.userMessage = userMessage;
        this.createdAt = createdAt;
        this.persistInHistory = persistInHistory;
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

    public String getAssistantMessage() {
        return assistantMessage;
    }

    public void setAssistantMessage(String assistantMessage) {
        contextLog.info("Adding assistant message to turn | correlationId={}", correlationId);
        if (contextLog.isDebugEnabled()) {
            contextLog.debug(
                    "correlationId={} assistantMessage={}", correlationId, assistantMessage);
        }
        this.assistantMessage = assistantMessage;
    }

    public boolean isAssistantReplyGenerated() {
        return assistantReplyGenerated;
    }

    public void setAssistantReplyGenerated(boolean assistantReplyGenerated) {
        contextLog.debug(
                "correlationId={} assistantReplyGenerated={}",
                correlationId,
                assistantReplyGenerated);
        this.assistantReplyGenerated = assistantReplyGenerated;
    }

    public boolean isAssistantPlaybackStarted() {
        return assistantPlaybackStarted;
    }

    public void setAssistantPlaybackStarted(boolean assistantPlaybackStarted) {
        contextLog.debug(
                "correlationId={} assistantPlaybackStarted={}",
                correlationId,
                assistantPlaybackStarted);
        this.assistantPlaybackStarted = assistantPlaybackStarted;
    }

    public boolean isAssistantPlaybackCompleted() {
        return assistantPlaybackCompleted;
    }

    public void setAssistantPlaybackCompleted(boolean assistantPlaybackCompleted) {
        contextLog.debug(
                "correlationId={} assistantPlaybackCompleted={}",
                correlationId,
                assistantPlaybackCompleted);
        this.assistantPlaybackCompleted = assistantPlaybackCompleted;
    }

    public boolean isAssistantPlaybackInterrupted() {
        return assistantPlaybackInterrupted;
    }

    public void setAssistantPlaybackInterrupted(boolean assistantPlaybackInterrupted) {
        contextLog.debug(
                "correlationId={} assistantPlaybackInterrupted={}",
                correlationId,
                assistantPlaybackInterrupted);
        this.assistantPlaybackInterrupted = assistantPlaybackInterrupted;
    }

    public boolean isSupersededBeforeAssistantReply() {
        return supersededBeforeAssistantReply;
    }

    public void setSupersededBeforeAssistantReply(boolean supersededBeforeAssistantReply) {
        contextLog.debug(
                "correlationId={} supersededBeforeAssistantReply={}",
                correlationId,
                supersededBeforeAssistantReply);
        this.supersededBeforeAssistantReply = supersededBeforeAssistantReply;
    }

    public String getSupersededByCorrelationId() {
        return supersededByCorrelationId;
    }

    public void setSupersededByCorrelationId(String supersededByCorrelationId) {
        contextLog.debug(
                "correlationId={} supersededByCorrelationId={}",
                correlationId,
                supersededByCorrelationId);
        this.supersededByCorrelationId = supersededByCorrelationId;
    }

    public boolean isPersistInHistory() {
        return persistInHistory;
    }
}
