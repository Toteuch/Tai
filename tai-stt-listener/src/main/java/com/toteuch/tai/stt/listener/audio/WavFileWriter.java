package com.toteuch.tai.stt.listener.audio;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class WavFileWriter {
    public void write(Path outputFile, byte[] audioBytes, AudioFormat audioFormat) {
        try {
            Files.createDirectories(outputFile.getParent());

            try (ByteArrayInputStream byteStream = new ByteArrayInputStream(audioBytes);
                 AudioInputStream audioInputStream = new AudioInputStream(
                     byteStream,
                     audioFormat,
                     audioBytes.length / audioFormat.getFrameSize()
                 )) {
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, outputFile.toFile());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write WAV file: " + outputFile, e);
        }
    }
}
