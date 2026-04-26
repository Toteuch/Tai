package com.toteuch.tai.orchestrator.core.handler.internal;

import com.toteuch.tai.orchestrator.core.EventHandler;
import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.orchestrator.events.EventSource;
import com.toteuch.tai.orchestrator.events.EventType;
import com.toteuch.tai.orchestrator.events.internal.AssistantReplyAcceptedEvent;
import com.toteuch.tai.orchestrator.events.internal.ConversationTurnCompletedEvent;
import com.toteuch.tai.orchestrator.services.tts.TtsClient;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SessionStore;
import com.toteuch.tai.orchestrator.session.SpeakingState;
import com.toteuch.tai.orchestrator.session.ThinkingState;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AssistantReplyAcceptedEventHandler
        implements EventHandler<AssistantReplyAcceptedEvent> {
    private static final Logger perfLog = LoggerFactory.getLogger("tai.performance");
    private static final Logger contextLog = LoggerFactory.getLogger("tai.context");
    private static final Logger traceLog = LoggerFactory.getLogger("tai.trace");

    private final SessionStore sessionStore;
    private final TaiEventPublisher eventPublisher;
    private final TtsClient ttsClient;

    public AssistantReplyAcceptedEventHandler(
            SessionStore sessionStore, TaiEventPublisher eventPublisher, TtsClient ttsClient) {
        this.sessionStore = sessionStore;
        this.eventPublisher = eventPublisher;
        this.ttsClient = ttsClient;
    }

    @Override
    public EventType supports() {
        return EventType.ASSISTANT_REPLY_ACCEPTED;
    }

    @Override
    public void handle(AssistantReplyAcceptedEvent event) {
        SessionContext sessionContext = sessionStore.get();

        String sanitizedResponseText = sanitizeText(event.replyText());
        traceLog.debug("Raw assistant reply sanitized");
        traceLog.debug("    rawMessage={}", event.replyText());
        traceLog.debug("    sanitizedMessage={}", sanitizedResponseText);

        sessionContext.getActiveTurn().setAssistantMessage(sanitizedResponseText);
        sessionContext.getActiveTurn().setAssistantReplyGenerated(true);
        contextLog.info(
                "New assistantMessage | correlationId={} assistantMessage={}",
                event.correlationId(),
                sanitizedResponseText);

        sessionContext.setThinkingState(ThinkingState.IDLE);

        if (sessionContext.isTtsEnabled()) {
            sessionContext.setSpeakingState(SpeakingState.PREPARING);

            perfLog.info("TTS speech called | correlationId={}", event.correlationId());
            ttsClient.speak(event.correlationId(), sanitizedResponseText);
        } else {
            eventPublisher.publish(
                    new ConversationTurnCompletedEvent(
                            UUID.randomUUID().toString(),
                            Instant.now(),
                            event.correlationId(),
                            EventSource.ORCHESTRATOR));
        }
    }

    private String sanitizeText(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }

        String sanitized = text;

        // Remove roleplay / stage directions between asterisks
        sanitized = sanitized.replaceAll("\\*[^*]{1,80}\\*", " ");

        // Remove repeated whitespace
        sanitized = sanitized.replaceAll("\\s+", " ");

        // Remove extra spaces before punctuation
        sanitized = sanitized.replaceAll("\\s+([,.;!?])", "$1");

        // Normalize repeated punctuation
        sanitized = sanitized.replaceAll("!{2,}", "!");
        sanitized = sanitized.replaceAll("\\?{2,}", "?");
        sanitized = sanitized.replaceAll("\\.{4,}", "...");

        // Clean orphan punctuation caused by removed stage directions
        sanitized = sanitized.replaceAll("(^|\\s)[,.;:!?](\\s|$)", " ");

        // Final whitespace cleanup
        sanitized = sanitized.replaceAll("\\s+", " ").trim();

        // Normalize curly apostrophes and quotes
        sanitized = sanitized.replace("’", "'");
        sanitized = sanitized.replace("“", "\"");
        sanitized = sanitized.replace("”", "\"");

        // Replace dash-style pauses with sentence boundaries
        sanitized = sanitized.replace(" - ", ". ");
        sanitized = sanitized.replace(" – ", ". ");
        sanitized = sanitized.replace(" — ", ". ");

        return sanitized;
    }
}
