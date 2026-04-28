package com.toteuch.tai.orchestrator.transport.debug;

import com.toteuch.tai.orchestrator.services.health.SystemHealthAggregator;
import com.toteuch.tai.orchestrator.transport.debug.dto.SystemHealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debug/system")
public class SystemHealthDebugController {
    private final SystemHealthAggregator systemHealthAggregator;

    public SystemHealthDebugController(SystemHealthAggregator systemHealthAggregator) {
        this.systemHealthAggregator = systemHealthAggregator;
    }

    @GetMapping("/health")
    public SystemHealthResponse health() {
        return systemHealthAggregator.aggregate();
    }
}
