package com.toteuch.tai.taiorchestrator.services.stt.audio.capture;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tai.stt.capture")
public class MicrophoneCaptureProperties {

    private String outputDir = "./tai-stt/input";
    private float sampleRate = 16000.0f;
    private int sampleSizeBits = 16;
    private int channels = 1;
    private boolean signed = true;
    private boolean bigEndian = false;
    private int bufferSize = 4096;
    private int silenceThreshold = 500;
    private int silenceDurationMs = 1200;
    private int minRecordingMs = 800;

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public float getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(float sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getSampleSizeBits() {
        return sampleSizeBits;
    }

    public void setSampleSizeBits(int sampleSizeBits) {
        this.sampleSizeBits = sampleSizeBits;
    }

    public int getChannels() {
        return channels;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    public boolean isSigned() {
        return signed;
    }

    public void setSigned(boolean signed) {
        this.signed = signed;
    }

    public boolean isBigEndian() {
        return bigEndian;
    }

    public void setBigEndian(boolean bigEndian) {
        this.bigEndian = bigEndian;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getSilenceThreshold() {
        return silenceThreshold;
    }

    public void setSilenceThreshold(int silenceThreshold) {
        this.silenceThreshold = silenceThreshold;
    }

    public int getSilenceDurationMs() {
        return silenceDurationMs;
    }

    public void setSilenceDurationMs(int silenceDurationMs) {
        this.silenceDurationMs = silenceDurationMs;
    }

    public int getMinRecordingMs() {
        return minRecordingMs;
    }

    public void setMinRecordingMs(int minRecordingMs) {
        this.minRecordingMs = minRecordingMs;
    }
}
