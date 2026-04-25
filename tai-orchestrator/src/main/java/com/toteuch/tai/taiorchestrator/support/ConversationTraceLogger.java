package com.toteuch.tai.taiorchestrator.support;

import com.toteuch.tai.taiorchestrator.services.llm.LlmMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConversationTraceLogger {

    private static final Logger log = LoggerFactory.getLogger("tai.conversation");

    private final TaiLoggingProperties loggingProperties;

    public ConversationTraceLogger(TaiLoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }

    public void logTurnStart(String sessionId, String correlationId, String userText) {
        System.out.println("ConversationTraceLogger.logTurnStart called");
        if (!loggingProperties.isConversationTraceEnabled()) {
            return;
        }

        log.info("""
            === TAI TURN START ===
            sessionId={}
            correlationId={}
            userText={}
            """.strip(), sessionId, correlationId, userText);
    }

    public void logLlmContext(String sessionId, String correlationId, List<LlmMessage> messages) {
        if (!loggingProperties.isConversationTraceEnabled() || !loggingProperties.isLogLlmContext()) {
            return;
        }

        if (loggingProperties.isLogLlmContextFull()) {
            log.info("""
                --- LLM CONTEXT START ---
                sessionId={}
                correlationId={}
                {}
                --- LLM CONTEXT END ---
                """.strip(), sessionId, correlationId, formatFullContext(messages));
        } else {
            log.info("""
                    --- LLM CONTEXT SUMMARY ---
                    sessionId={}
                    correlationId={}
                    messageCount={}
                    roles={}
                    --- LLM CONTEXT SUMMARY END ---
                    """.strip(),
                sessionId,
                correlationId,
                messages.size(),
                messages.stream().map(LlmMessage::role).toList());
        }
    }

    public void logLlmReply(String sessionId, String correlationId, String model, long durationMs, String replyText) {
        if (!loggingProperties.isConversationTraceEnabled()) {
            return;
        }

        log.info("""
            --- LLM REPLY ---
            sessionId={}
            correlationId={}
            model={}
            durationMs={}
            assistantReply={}
            """.strip(), sessionId, correlationId, model, durationMs, replyText);
    }

    public void logLlmFailure(String sessionId, String correlationId, String errorCode, String errorMessage) {
        if (!loggingProperties.isConversationTraceEnabled()) {
            return;
        }

        log.info("""
            --- LLM FAILURE ---
            sessionId={}
            correlationId={}
            errorCode={}
            errorMessage={}
            """.strip(), sessionId, correlationId, errorCode, errorMessage);
    }

    public void logTurnEnd(String sessionId, String correlationId, boolean ttsEnabled) {
        if (!loggingProperties.isConversationTraceEnabled()) {
            return;
        }

        log.info("""
            === TAI TURN END ===
            sessionId={}
            correlationId={}
            ttsEnabled={}
            """.strip(), sessionId, correlationId, ttsEnabled);
    }

    private String formatFullContext(List<LlmMessage> messages) {
        StringBuilder sb = new StringBuilder();

        for (LlmMessage message : messages) {
            sb.append('[')
                .append(message.role())
                .append(']')
                .append(System.lineSeparator())
                .append(message.content())
                .append(System.lineSeparator())
                .append(System.lineSeparator());
        }

        return sb.toString().trim();
    }
}
