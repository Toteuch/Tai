package com.toteuch.tai.orchestrator.services.tts.piper;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tai.tts.piper")
public class PiperTtsProperties {

    private boolean enabled = true;
    private String executable;
    private String model;
    private String config;
    private String outputDir;
    private String voiceId;
    private double sentenceSilence = 0.15;
    private double lengthScale = 1.0;
    private double noiseScale = 0.667;
    private double noiseWScale = 0.8;
    private String segmentationMode = "single";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

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

    public double getSentenceSilence() {
        return sentenceSilence;
    }

    public void setSentenceSilence(double sentenceSilence) {
        this.sentenceSilence = sentenceSilence;
    }

    public double getLengthScale() {
        return lengthScale;
    }

    public void setLengthScale(double lengthScale) {
        this.lengthScale = lengthScale;
    }

    public double getNoiseScale() {
        return noiseScale;
    }

    public void setNoiseScale(double noiseScale) {
        this.noiseScale = noiseScale;
    }

    public double getNoiseWScale() {
        return noiseWScale;
    }

    public void setNoiseWScale(double noiseWScale) {
        this.noiseWScale = noiseWScale;
    }

    public String getSegmentationMode() {
        return segmentationMode;
    }

    public void setSegmentationMode(String segmentationMode) {
        this.segmentationMode = segmentationMode;
    }
}
