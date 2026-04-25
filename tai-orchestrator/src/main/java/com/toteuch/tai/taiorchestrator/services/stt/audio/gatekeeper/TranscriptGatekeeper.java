package com.toteuch.tai.taiorchestrator.services.stt.audio.gatekeeper;

import com.toteuch.tai.taiorchestrator.services.stt.SttResult;
import com.toteuch.tai.taiorchestrator.services.stt.audio.vad.SpeechSegment;

public interface TranscriptGatekeeper {

    GatekeeperDecision evaluate(SpeechSegment segment, SttResult sttResult);
}
