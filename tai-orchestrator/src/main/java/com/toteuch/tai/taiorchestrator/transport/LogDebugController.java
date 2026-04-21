package com.toteuch.tai.taiorchestrator.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debug")
public class LogDebugController {

    private static final Logger log = LoggerFactory.getLogger("tai.conversation");

    @PostMapping("/conversation-log-test")
    public String testLog() {
        log.info("TEST tai.conversation logger");
        return "OK";
    }
}
