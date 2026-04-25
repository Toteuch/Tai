package com.toteuch.tai.stt.listener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tai.stt")
public class SttListenerProperties {
    private Capture capture = new Capture();

    public Capture getCapture() { return capture; }
    public void setCapture(Capture capture) { this.capture = capture; }

    public static class Capture {
        private String outputDir = "./input";
        private float sampleRate = 16000;
        private int sampleSizeBits = 16;
        private int channels = 1;
        private boolean signed = true;
        private boolean bigEndian = false;
        private int bufferSize = 4096;
        private double silenceThreshold = 40;
        private long silenceDurationMs = 1200;
        private long minRecordingMs = 800;
        private long maxRecordingMs = 15000;
        private long noSpeechTimeoutMs = 3000;

        public String getOutputDir() { return outputDir; }
        public void setOutputDir(String outputDir) { this.outputDir = outputDir; }
        public float getSampleRate() { return sampleRate; }
        public void setSampleRate(float sampleRate) { this.sampleRate = sampleRate; }
        public int getSampleSizeBits() { return sampleSizeBits; }
        public void setSampleSizeBits(int sampleSizeBits) { this.sampleSizeBits = sampleSizeBits; }
        public int getChannels() { return channels; }
        public void setChannels(int channels) { this.channels = channels; }
        public boolean isSigned() { return signed; }
        public void setSigned(boolean signed) { this.signed = signed; }
        public boolean isBigEndian() { return bigEndian; }
        public void setBigEndian(boolean bigEndian) { this.bigEndian = bigEndian; }
        public int getBufferSize() { return bufferSize; }
        public void setBufferSize(int bufferSize) { this.bufferSize = bufferSize; }
        public double getSilenceThreshold() { return silenceThreshold; }
        public void setSilenceThreshold(double silenceThreshold) { this.silenceThreshold = silenceThreshold; }
        public long getSilenceDurationMs() { return silenceDurationMs; }
        public void setSilenceDurationMs(long silenceDurationMs) { this.silenceDurationMs = silenceDurationMs; }
        public long getMinRecordingMs() { return minRecordingMs; }
        public void setMinRecordingMs(long minRecordingMs) { this.minRecordingMs = minRecordingMs; }
        public long getMaxRecordingMs() { return maxRecordingMs; }
        public void setMaxRecordingMs(long maxRecordingMs) { this.maxRecordingMs = maxRecordingMs; }
        public long getNoSpeechTimeoutMs() { return noSpeechTimeoutMs; }
        public void setNoSpeechTimeoutMs(long noSpeechTimeoutMs) { this.noSpeechTimeoutMs = noSpeechTimeoutMs; }
    }
}
