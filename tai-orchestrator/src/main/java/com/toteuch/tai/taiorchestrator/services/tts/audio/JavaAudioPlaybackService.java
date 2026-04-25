package com.toteuch.tai.taiorchestrator.services.tts.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Path;

@Component
public class JavaAudioPlaybackService {

    private static final Logger log = LoggerFactory.getLogger(JavaAudioPlaybackService.class);

    public long playBlocking(Path wavPath, WavPlaybackHandle handle) throws Exception {
        log.info("JavaAudioPlaybackService playing wav={}", wavPath.toAbsolutePath());

        try (
            AudioInputStream originalStream = AudioSystem.getAudioInputStream(new BufferedInputStream(java.nio.file.Files.newInputStream(wavPath)))
        ) {
            AudioFormat baseFormat = originalStream.getFormat();

            AudioFormat decodedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false
            );

            try (
                AudioInputStream decodedStream = AudioSystem.getAudioInputStream(decodedFormat, originalStream)
            ) {
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
                try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                    handle.setLine(line);

                    line.open(decodedFormat);
                    line.start();

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long totalBytesWritten = 0L;

                    while (!handle.isStopRequested() && (bytesRead = decodedStream.read(buffer, 0, buffer.length)) != -1) {
                        int written = line.write(buffer, 0, bytesRead);
                        totalBytesWritten += Math.max(written, 0);
                    }

                    if (!handle.isStopRequested()) {
                        line.drain();
                    }

                    long frameSize = decodedFormat.getFrameSize();
                    float frameRate = decodedFormat.getFrameRate();

                    if (frameSize <= 0 || frameRate <= 0) {
                        return -1L;
                    }

                    long framesWritten = totalBytesWritten / frameSize;
                    return (long) ((framesWritten * 1000.0) / frameRate);
                }
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            log.error("Audio playback failed for wav={}", wavPath.toAbsolutePath(), e);
            throw e;
        }
    }
}
