package com.toteuch.tai.stt.listener.capture;

import com.toteuch.tai.stt.listener.audio.AudioMetrics;
import com.toteuch.tai.stt.listener.audio.SpeechSegment;
import com.toteuch.tai.stt.listener.audio.WavFileWriter;
import com.toteuch.tai.stt.listener.config.SttListenerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class MicrophoneCaptureService {
    private static final Logger log = LoggerFactory.getLogger(MicrophoneCaptureService.class);

    private final SttListenerProperties properties;
    private final WavFileWriter wavFileWriter = new WavFileWriter();

    public MicrophoneCaptureService(SttListenerProperties properties) {
        this.properties = properties;
    }

    public SpeechSegment captureOnce() {
        SttListenerProperties.Capture capture = properties.getCapture();
        AudioFormat audioFormat = audioFormat(capture);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);

        if (!AudioSystem.isLineSupported(info)) {
            throw new IllegalStateException("Microphone line is not supported for format: " + audioFormat);
        }

        Path outputFile = Path.of(capture.getOutputDir(), "mic_" + System.currentTimeMillis() + ".wav");
        ByteArrayOutputStream audioBuffer = new ByteArrayOutputStream();
        List<Double> energies = new ArrayList<>();

        boolean speechStarted = false;
        boolean speechEnded = false;

        long startedAt = System.nanoTime();
        long lastVoiceAt = startedAt;

        try (TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info)) {
            microphone.open(audioFormat, capture.getBufferSize());
            microphone.start();

            byte[] buffer = new byte[capture.getBufferSize()];

            while (true) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);

                if (bytesRead <= 0) {
                    continue;
                }

                audioBuffer.write(buffer, 0, bytesRead);

                byte[] chunk = new byte[bytesRead];
                System.arraycopy(buffer, 0, chunk, 0, bytesRead);

                double energy = AudioMetrics.averageAbsoluteEnergy(
                    chunk,
                    capture.getSampleSizeBits(),
                    capture.isBigEndian()
                );
                energies.add(energy);

                long now = System.nanoTime();
                long elapsedMs = elapsedMs(startedAt, now);

                if (energy >= capture.getSilenceThreshold()) {
                    speechStarted = true;
                    lastVoiceAt = now;
                }

                if (!speechStarted && elapsedMs >= capture.getNoSpeechTimeoutMs()) {
                    log.debug("Stopping capture because no speech was detected");
                    break;
                }

                long silenceMs = elapsedMs(lastVoiceAt, now);

                if (
                    speechStarted
                        && elapsedMs >= capture.getMinRecordingMs()
                        && silenceMs >= capture.getSilenceDurationMs()
                ) {
                    speechEnded = true;
                    break;
                }

                if (elapsedMs >= capture.getMaxRecordingMs()) {
                    break;
                }
            }

            microphone.stop();
        } catch (Exception e) {
            throw new IllegalStateException("Microphone capture failed", e);
        }

        byte[] audioBytes = audioBuffer.toByteArray();
        wavFileWriter.write(outputFile, audioBytes, audioFormat);

        long durationMs = durationMs(audioBytes, audioFormat);
        double averageEnergy = AudioMetrics.average(energies);
        double peakEnergy = AudioMetrics.max(energies);
        double voicedRatio = AudioMetrics.voicedRatio(energies, capture.getSilenceThreshold());

        return new SpeechSegment(
            outputFile,
            durationMs,
            averageEnergy,
            peakEnergy,
            voicedRatio,
            speechStarted,
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
