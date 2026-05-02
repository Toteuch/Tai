// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.transport;

import com.toteuch.tai.orchestrator.ui.model.TaiUiState;
import com.toteuch.tai.orchestrator.ui.projection.TaiUiStateProjectionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ui/state")
public class UiStateController {

    private final TaiUiStateProjectionService taiUiStateProjectionService;

    public UiStateController(TaiUiStateProjectionService taiUiStateProjectionService) {
        this.taiUiStateProjectionService = taiUiStateProjectionService;
    }

    @GetMapping
    public TaiUiState getCurrentState() {
        return taiUiStateProjectionService.currentOrRebuild();
    }
}
