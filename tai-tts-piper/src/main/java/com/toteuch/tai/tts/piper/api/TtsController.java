package com.toteuch.tai.tts.piper.api;

import com.toteuch.tai.tts.piper.api.dto.TtsAcceptedResponse;
import com.toteuch.tai.tts.piper.api.dto.TtsSpeakRequest;
import com.toteuch.tai.tts.piper.api.dto.TtsStopRequest;
import com.toteuch.tai.tts.piper.service.TtsPlaybackService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tts")
public class TtsController {
    private final TtsPlaybackService playbackService;

    public TtsController(TtsPlaybackService playbackService) {
        this.playbackService = playbackService;
    }

    @PostMapping("/speak")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Start asynchronous TTS playback")
    public TtsAcceptedResponse speak(@Valid @RequestBody TtsSpeakRequest request) {
        playbackService.speakAsync(request.correlationId(), request.text());
        return new TtsAcceptedResponse(true, request.correlationId());
    }

    @PostMapping("/stop")
    @Operation(summary = "Stop current TTS playback")
    public TtsAcceptedResponse stop(@Valid @RequestBody TtsStopRequest request) {
        playbackService.stop(request.correlationId());
        return new TtsAcceptedResponse(true, request.correlationId());
    }
}
