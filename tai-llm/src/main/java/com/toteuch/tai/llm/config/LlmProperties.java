// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.llm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tai.llm")
public class LlmProperties {
    private Async async = new Async();
    private Orchestrator orchestrator = new Orchestrator();
    private Ollama ollama = new Ollama();

    public Async getAsync() {
        return async;
    }

    public void setAsync(Async async) {
        this.async = async;
    }

    public Orchestrator getOrchestrator() {
        return orchestrator;
    }

    public void setOrchestrator(Orchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    public Ollama getOllama() {
        return ollama;
    }

    public void setOllama(Ollama ollama) {
        this.ollama = ollama;
    }

    public static class Async {
        private int corePoolSize = 1, maxPoolSize = 2, queueCapacity = 50;

        public int getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(int v) {
            corePoolSize = v;
        }

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(int v) {
            maxPoolSize = v;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int v) {
            queueCapacity = v;
        }
    }

    public static class Orchestrator {
        private String baseUrl = "http://localhost:8080";
        private int connectTimeoutMs = 3000, readTimeoutMs = 10000;
        private Callbacks callbacks = new Callbacks();

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String v) {
            baseUrl = v;
        }

        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(int v) {
            connectTimeoutMs = v;
        }

        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(int v) {
            readTimeoutMs = v;
        }

        public Callbacks getCallbacks() {
            return callbacks;
        }

        public void setCallbacks(Callbacks v) {
            callbacks = v;
        }

        public static class Callbacks {
            private String responseCompletedPath = "/events/llm/response-completed";
            private String responseFailedPath = "/events/llm/response-failed";

            public String getResponseCompletedPath() {
                return responseCompletedPath;
            }

            public void setResponseCompletedPath(String v) {
                responseCompletedPath = v;
            }

            public String getResponseFailedPath() {
                return responseFailedPath;
            }

            public void setResponseFailedPath(String v) {
                responseFailedPath = v;
            }
        }
    }

    public static class Ollama {
        private String baseUrl = "http://localhost:11434",
                chatPath = "/api/chat",
                tagsPath = "/api/tags",
                model = "tai-llama",
                keepAlive = "-1",
                warmUpPrompt = "Say ready.";
        private boolean stream = false, warmUpOnStartup = true;
        private int connectTimeoutMs = 3000, readTimeoutMs = 120000;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String v) {
            baseUrl = v;
        }

        public String getChatPath() {
            return chatPath;
        }

        public void setChatPath(String v) {
            chatPath = v;
        }

        public String getTagsPath() {
            return tagsPath;
        }

        public void setTagsPath(String v) {
            tagsPath = v;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String v) {
            model = v;
        }

        public boolean isStream() {
            return stream;
        }

        public void setStream(boolean v) {
            stream = v;
        }

        public String getKeepAlive() {
            return keepAlive;
        }

        public void setKeepAlive(String v) {
            keepAlive = v;
        }

        public boolean isWarmUpOnStartup() {
            return warmUpOnStartup;
        }

        public void setWarmUpOnStartup(boolean v) {
            warmUpOnStartup = v;
        }

        public String getWarmUpPrompt() {
            return warmUpPrompt;
        }

        public void setWarmUpPrompt(String v) {
            warmUpPrompt = v;
        }

        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(int v) {
            connectTimeoutMs = v;
        }

        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(int v) {
            readTimeoutMs = v;
        }
    }
}
