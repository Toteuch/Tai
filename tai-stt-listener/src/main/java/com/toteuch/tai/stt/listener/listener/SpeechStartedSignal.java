// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.stt.listener.listener;

public record SpeechStartedSignal(double averageEnergy, double peakEnergy) {}
