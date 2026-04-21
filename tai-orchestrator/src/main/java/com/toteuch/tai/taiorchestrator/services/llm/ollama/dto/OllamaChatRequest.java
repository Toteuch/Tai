package com.toteuch.tai.taiorchestrator.services.llm.ollama.dto;

import java.util.List;
import java.util.Map;

public class OllamaChatRequest {

    private String model;
    private List<MessageDto> messages;
    private boolean stream;
    private String keepAlive;
    private Map<String, Object> options;

    public OllamaChatRequest() {
    }

    public OllamaChatRequest(
        String model,
        List<MessageDto> messages,
        boolean stream,
        String keepAlive,
        Map<String, Object> options
    ) {
        this.model = model;
        this.messages = messages;
        this.stream = stream;
        this.keepAlive = keepAlive;
        this.options = options;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<MessageDto> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageDto> messages) {
        this.messages = messages;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public String getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(String keepAlive) {
        this.keepAlive = keepAlive;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    public static class MessageDto {
        private String role;
        private String content;

        public MessageDto() {
        }

        public MessageDto(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
