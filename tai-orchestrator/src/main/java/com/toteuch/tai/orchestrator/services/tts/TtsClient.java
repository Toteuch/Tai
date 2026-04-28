// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.services.tts;

public interface TtsClient {
    void speak(String correlationId, String text);

    void stop(String correlationId);
}
