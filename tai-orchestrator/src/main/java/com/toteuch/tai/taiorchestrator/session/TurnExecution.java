package com.toteuch.tai.taiorchestrator.session;

import java.time.Instant;

/**
 * Represents the runtime execution state of the currently active conversational turn.
 *
 * <p>Unlike {@link ConversationTurn}, which stores conversational memory,
 * this class represents execution authority at runtime. It identifies which request
 * is currently allowed to affect the system state, UI, and downstream services
 * such as TTS.</p>
 *
 * <p>This class is used to protect the orchestrator against stale or superseded
 * results. For example, if a first LLM generation finishes after a second user input
 * has already started a newer turn, the old result must be ignored. The active
 * {@code TurnExecution} is the source of truth for that decision.</p>
 *
 * <p>A turn execution may move through several states such as active, completed,
 * superseded, or failed. It also stores enough metadata to trace which user input
 * launched the execution and which newer execution replaced it, if any.</p>
 *
 * <p>In short:
 * <ul>
 *     <li>{@code ConversationTurn} answers: "What happened in the conversation?"</li>
 *     <li>{@code TurnExecution} answers: "What execution is currently authoritative?"</li>
 * </ul>
 * </p>
 */
public class TurnExecution {

    private final String requestId;
    private final String correlationId;
    private final String turnCorrelationId;
    private final String userText;
    private final Instant createdAt;

    private TurnExecutionStatus status;
    private String supersededByCorrelationId;

    public TurnExecution(
        String requestId,
        String correlationId,
        String turnCorrelationId,
        String userText,
        Instant createdAt,
        TurnExecutionStatus status
    ) {
        this.requestId = requestId;
        this.correlationId = correlationId;
        this.turnCorrelationId = turnCorrelationId;
        this.userText = userText;
        this.createdAt = createdAt;
        this.status = status;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getTurnCorrelationId() {
        return turnCorrelationId;
    }

    public String getUserText() {
        return userText;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public TurnExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(TurnExecutionStatus status) {
        this.status = status;
    }

    public String getSupersededByCorrelationId() {
        return supersededByCorrelationId;
    }

    public void setSupersededByCorrelationId(String supersededByCorrelationId) {
        this.supersededByCorrelationId = supersededByCorrelationId;
    }

    public boolean isActive() {
        return status == TurnExecutionStatus.ACTIVE;
    }
}
