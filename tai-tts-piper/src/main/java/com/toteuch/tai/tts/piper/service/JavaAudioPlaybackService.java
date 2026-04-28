// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.tts.piper.service;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JavaAudioPlaybackService {
    private static final Logger log = LoggerFactory.getLogger(JavaAudioPlaybackService.class);

    private final AtomicReference<Clip> currentClip = new AtomicReference<>();

    public void playBlocking(Path wavFile) {
        CountDownLatch playbackFinished = new CountDownLatch(1);
        Clip clip = null;

        try (AudioInputStream audioInputStream =
                AudioSystem.getAudioInputStream(wavFile.toFile())) {
            log.info("Opening WAV playback | file={}", wavFile.toAbsolutePath());

            clip = AudioSystem.getClip();
            Clip finalClip = clip;

            clip.addLineListener(
                    event -> {
                        if (event.getType() == LineEvent.Type.STOP
                                || event.getType() == LineEvent.Type.CLOSE) {
                            playbackFinished.countDown();
                        }
                    });

            clip.open(audioInputStream);
            currentClip.set(clip);

            log.info(
                    "Starting WAV playback | file={} frameLength={} microsecondLength={}",
                    wavFile.toAbsolutePath(),
                    clip.getFrameLength(),
                    clip.getMicrosecondLength());

            clip.start();

            playbackFinished.await();

            log.info("WAV playback finished | file={}", wavFile.toAbsolutePath());

            currentClip.compareAndSet(finalClip, null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            stop();
        } catch (Exception e) {
            throw new IllegalStateException("Audio playback failed", e);
        } finally {
            if (clip != null) {
                clip.close();
            }
        }
    }

    public void stop() {
        Clip clip = currentClip.getAndSet(null);

        if (clip != null) {
            log.info("Stopping WAV playback");
            clip.stop();
            clip.flush();
            clip.close();
        }
    }
}
