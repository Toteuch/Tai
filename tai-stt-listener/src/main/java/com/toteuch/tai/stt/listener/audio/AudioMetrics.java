// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.stt.listener.audio;

import java.util.List;

public final class AudioMetrics {
    private AudioMetrics() {}

    public static double averageAbsoluteEnergy(
            byte[] audio, int sampleSizeBits, boolean bigEndian) {
        if (audio == null || audio.length == 0) {
            return 0.0;
        }

        if (sampleSizeBits != 16) {
            throw new IllegalArgumentException("Only 16-bit PCM is supported for now");
        }

        long sum = 0;
        int samples = audio.length / 2;

        for (int i = 0; i + 1 < audio.length; i += 2) {
            short sample = readSample(audio[i], audio[i + 1], bigEndian);
            sum += Math.abs(sample);
        }

        return samples == 0 ? 0.0 : (double) sum / samples;
    }

    public static double average(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }

        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    public static double max(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }

        return values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
    }

    public static double voicedRatio(List<Double> energies, double threshold) {
        if (energies == null || energies.isEmpty()) {
            return 0.0;
        }

        long voicedChunks = energies.stream().filter(energy -> energy >= threshold).count();

        return (double) voicedChunks / energies.size();
    }

    private static short readSample(byte lowOrHigh, byte highOrLow, boolean bigEndian) {
        int b1 = lowOrHigh & 0xff;
        int b2 = highOrLow & 0xff;

        if (bigEndian) {
            return (short) ((b1 << 8) | b2);
        }

        return (short) ((b2 << 8) | b1);
    }
}
