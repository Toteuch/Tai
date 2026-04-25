package com.toteuch.tai.stt.listener.listener;

import com.toteuch.tai.stt.listener.audio.AudioMetrics;
import com.toteuch.tai.stt.listener.audio.SpeechSegment;
import com.toteuch.tai.stt.listener.audio.WavFileWriter;
import com.toteuch.tai.stt.listener.config.SttListenerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

@Service
public class SpeechSegmentRecorder {
    private static final Logger log = LoggerFactory.getLogger(SpeechSegmentRecorder.class);

    private final SttListenerProperties properties;
    private final WavFileWriter wavFileWriter = new WavFileWriter();

    public SpeechSegmentRecorder(SttListenerProperties properties) {
        this.properties = properties;
    }

    public TargetDataLine openMicrophoneLine() {
        SttListenerProperties.Capture capture = properties.getCapture();
        AudioFormat audioFormat = audioFormat(capture);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);

        if (!AudioSystem.isLineSupported(info)) {
            throw new IllegalStateException("Microphone line is not supported for format: " + audioFormat);
        }

        try {
            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(audioFormat, capture.getBufferSize());
            microphone.start();

            log.info("Microphone line opened for continuous listening | format={}", audioFormat);

            return microphone;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to open microphone line", e);
        }
    }

    public SpeechSegment recordNextSegment(TargetDataLine microphone, BooleanSupplier running) {
        SttListenerProperties.Capture capture = properties.getCapture();
        AudioFormat audioFormat = audioFormat(capture);

        ByteArrayOutputStream audioBuffer = new ByteArrayOutputStream();
        List<Double> energies = new ArrayList<>();

        boolean speechStarted = false;
        boolean speechEnded = false;

        long segmentStartedAt = 0L;
        long lastVoiceAt = 0L;

        byte[] buffer = new byte[capture.getBufferSize()];

        try {
            while (running.getAsBoolean()) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);

                if (bytesRead <= 0) {
                    continue;
                }

                byte[] chunk = new byte[bytesRead];
                System.arraycopy(buffer, 0, chunk, 0, bytesRead);

                double energy = AudioMetrics.averageAbsoluteEnergy(
                    chunk,
                    capture.getSampleSizeBits(),
                    capture.isBigEndian()
                );

                long now = System.nanoTime();

                if (!speechStarted) {
                    if (energy >= capture.getSilenceThreshold()) {
                        speechStarted = true;
                        segmentStartedAt = now;
                        lastVoiceAt = now;

                        audioBuffer.write(chunk);
                        energies.add(energy);

                        log.info("Speech segment started | energy={}", energy);
                    }

                    continue;
                }

                audioBuffer.write(chunk);
                energies.add(energy);

                if (energy >= capture.getSilenceThreshold()) {
                    lastVoiceAt = now;
                }

                long elapsedMs = elapsedMs(segmentStartedAt, now);
                long silenceMs = elapsedMs(lastVoiceAt, now);

                if (
                    elapsedMs >= capture.getMinRecordingMs()
                        && silenceMs >= capture.getSilenceDurationMs()
                ) {
                    speechEnded = true;
                    break;
                }

                if (elapsedMs >= capture.getMaxRecordingMs()) {
                    log.info("Speech segment reached max duration | elapsedMs={}", elapsedMs);
                    break;
                }
            }
        } catch (Exception e) {
            if (!running.getAsBoolean()) {
                return null;
            }

            throw new IllegalStateException("Continuous speech recording failed", e);
        }

        if (!speechStarted || audioBuffer.size() == 0) {
            return null;
        }

        Path outputFile = Path.of(
            capture.getOutputDir(),
            "mic_" + System.currentTimeMillis() + ".wav"
        );

        byte[] audioBytes = audioBuffer.toByteArray();
        wavFileWriter.write(outputFile, audioBytes, audioFormat);

        long durationMs = durationMs(audioBytes, audioFormat);
        double averageEnergy = AudioMetrics.average(energies);
        double peakEnergy = AudioMetrics.max(energies);
        double voicedRatio = AudioMetrics.voicedRatio(energies, capture.getSilenceThreshold());

        log.info(
            "Speech segment ended | file={} durationMs={} averageEnergy={} peakEnergy={} voicedRatio={} speechEnded={}",
            outputFile,
            durationMs,
            averageEnergy,
            peakEnergy,
            voicedRatio,
            speechEnded
        );

        return new SpeechSegment(
            outputFile,
            durationMs,
            averageEnergy,
            peakEnergy,
            voicedRatio,
            true,
            speechEnded
        );
    }

    private AudioFormat audioFormat(SttListenerProperties.Capture capture) {
        return new AudioFormat(
            capture.getSampleRate(),
            capture.getSampleSizeBits(),
            capture.getChannels(),
            capture.isSigned(),
            capture.isBigEndian()
        );
    }

    private long durationMs(byte[] audioBytes, AudioFormat audioFormat) {
        if (audioBytes.length == 0) {
            return 0L;
        }

        long frames = audioBytes.length / audioFormat.getFrameSize();
        return Math.round((frames / audioFormat.getFrameRate()) * 1000.0);
    }

    private long elapsedMs(long startNanos, long endNanos) {
        return (endNanos - startNanos) / 1_000_000L;
    }
}
