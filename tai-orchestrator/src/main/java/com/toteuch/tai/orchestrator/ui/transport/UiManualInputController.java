// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.transport;

import com.toteuch.tai.orchestrator.ui.input.ManualInputService;
import com.toteuch.tai.orchestrator.ui.model.input.ManualInputRequest;
import com.toteuch.tai.orchestrator.ui.model.input.ManualInputResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ui/manual-input")
public class UiManualInputController {

    private final ManualInputService manualInputService;

    public UiManualInputController(ManualInputService manualInputService) {
        this.manualInputService = manualInputService;
    }

    @PostMapping
    public ManualInputResponse submitManualInput(@RequestBody ManualInputRequest request) {
        return manualInputService.submit(request);
    }
}
