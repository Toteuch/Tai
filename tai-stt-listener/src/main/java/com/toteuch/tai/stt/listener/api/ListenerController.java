// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.stt.listener.api;

import com.toteuch.tai.stt.listener.api.dto.ListenerControlResponse;
import com.toteuch.tai.stt.listener.listener.ContinuousListeningService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/listener")
public class ListenerController {
    private final ContinuousListeningService listeningService;

    public ListenerController(ContinuousListeningService listeningService) {
        this.listeningService = listeningService;
    }

    @PostMapping("/start")
    @Operation(
            summary = "Start continuous microphone listening",
            description =
                    "Opens the microphone and starts waiting for speech segments continuously.")
    public ListenerControlResponse start() {
        return ListenerControlResponse.from(listeningService.start());
    }

    @PostMapping("/stop")
    @Operation(
            summary = "Stop continuous microphone listening",
            description = "Stops the continuous listener and closes the microphone line.")
    public ListenerControlResponse stop() {
        return ListenerControlResponse.from(listeningService.stop());
    }
}
