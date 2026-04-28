// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.tts.piper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tai.tts")
public class TtsPiperProperties {
    private Async async = new Async();
    private Orchestrator orchestrator = new Orchestrator();
    private Piper piper = new Piper();

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

    public Piper getPiper() {
        return piper;
    }

    public void setPiper(Piper piper) {
        this.piper = piper;
    }

    public static class Async {
        private int corePoolSize = 1;
        private int maxPoolSize = 1;
        private int queueCapacity = 10;

        public int getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }
    }

    public static class Orchestrator {
        private String baseUrl = "http://localhost:8080";
        private int connectTimeoutMs = 3000;
        private int readTimeoutMs = 10000;
        private Callbacks callbacks = new Callbacks();

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }

        public Callbacks getCallbacks() {
            return callbacks;
        }

        public void setCallbacks(Callbacks callbacks) {
            this.callbacks = callbacks;
        }

        public static class Callbacks {
            private String playbackStartedPath = "/events/tts/playback-started";
            private String playbackCompletedPath = "/events/tts/playback-completed";
            private String playbackFailedPath = "/events/tts/playback-failed";

            public String getPlaybackStartedPath() {
                return playbackStartedPath;
            }

            public void setPlaybackStartedPath(String playbackStartedPath) {
                this.playbackStartedPath = playbackStartedPath;
            }

            public String getPlaybackCompletedPath() {
                return playbackCompletedPath;
            }

            public void setPlaybackCompletedPath(String playbackCompletedPath) {
                this.playbackCompletedPath = playbackCompletedPath;
            }

            public String getPlaybackFailedPath() {
                return playbackFailedPath;
            }

            public void setPlaybackFailedPath(String playbackFailedPath) {
                this.playbackFailedPath = playbackFailedPath;
            }
        }
    }

    public static class Piper {
        private String executable = "./.venv/Scripts/piper.exe";
        private String model = "./voices/en_GB-alba-medium.onnx";
        private String config = "./voices/en_GB-alba-medium.onnx.json";
        private String outputDir = "./output";
        private String voiceId = "en_GB-alba-medium";
        private long processTimeoutMs = 60000;

        public String getExecutable() {
            return executable;
        }

        public void setExecutable(String executable) {
            this.executable = executable;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getConfig() {
            return config;
        }

        public void setConfig(String config) {
            this.config = config;
        }

        public String getOutputDir() {
            return outputDir;
        }

        public void setOutputDir(String outputDir) {
            this.outputDir = outputDir;
        }

        public String getVoiceId() {
            return voiceId;
        }

        public void setVoiceId(String voiceId) {
            this.voiceId = voiceId;
        }

        public long getProcessTimeoutMs() {
            return processTimeoutMs;
        }

        public void setProcessTimeoutMs(long processTimeoutMs) {
            this.processTimeoutMs = processTimeoutMs;
        }
    }
}
