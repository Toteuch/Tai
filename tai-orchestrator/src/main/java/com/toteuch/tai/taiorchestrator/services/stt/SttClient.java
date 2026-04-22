package com.toteuch.tai.taiorchestrator.services.stt;

import java.nio.file.Path;

public interface SttClient {

    SttResult transcribe(String sessionId, Path audioFile);
}
