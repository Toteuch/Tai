package com.toteuch.tai.stt.listener.health;

import com.toteuch.tai.stt.listener.config.SttListenerProperties;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("microphoneCapture")
public class CaptureHealthIndicator implements HealthIndicator {
    private final SttListenerProperties properties;

    public CaptureHealthIndicator(SttListenerProperties properties) {
        this.properties = properties;
    }

    @Override
    public Health health() {
        AudioFormat format =
                new AudioFormat(
                        properties.getCapture().getSampleRate(),
                        properties.getCapture().getSampleSizeBits(),
                        properties.getCapture().getChannels(),
                        properties.getCapture().isSigned(),
                        properties.getCapture().isBigEndian());

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        Health.Builder builder = AudioSystem.isLineSupported(info) ? Health.up() : Health.down();

        return builder.withDetail("sampleRate", properties.getCapture().getSampleRate())
                .withDetail("sampleSizeBits", properties.getCapture().getSampleSizeBits())
                .withDetail("channels", properties.getCapture().getChannels())
                .withDetail("bufferSize", properties.getCapture().getBufferSize())
                .withDetail("outputDir", properties.getCapture().getOutputDir())
                .build();
    }
}
