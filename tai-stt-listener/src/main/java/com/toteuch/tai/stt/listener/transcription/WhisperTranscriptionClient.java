package com.toteuch.tai.stt.listener.transcription;

import java.nio.file.Path;

public interface WhisperTranscriptionClient {
    TranscriptionResult transcribe(String correlationId, Path audioFile);
}
