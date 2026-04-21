package com.toteuch.tai.taiorchestrator.support;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tai.logging")
public class TaiLoggingProperties {

    private boolean conversationTraceEnabled = true;
    private boolean logLlmContext = true;
    private boolean logLlmContextFull = false;

    public boolean isConversationTraceEnabled() {
        return conversationTraceEnabled;
    }

    public void setConversationTraceEnabled(boolean conversationTraceEnabled) {
        this.conversationTraceEnabled = conversationTraceEnabled;
    }

    public boolean isLogLlmContext() {
        return logLlmContext;
    }

    public void setLogLlmContext(boolean logLlmContext) {
        this.logLlmContext = logLlmContext;
    }

    public boolean isLogLlmContextFull() {
        return logLlmContextFull;
    }

    public void setLogLlmContextFull(boolean logLlmContextFull) {
        this.logLlmContextFull = logLlmContextFull;
    }
}
