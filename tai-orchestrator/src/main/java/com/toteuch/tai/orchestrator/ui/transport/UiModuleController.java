// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.transport;

import com.toteuch.tai.orchestrator.ui.details.ModuleDetailsService;
import com.toteuch.tai.orchestrator.ui.model.ModuleDetails;
import com.toteuch.tai.orchestrator.ui.model.TaiModule;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ui/modules")
public class UiModuleController {

    private final ModuleDetailsService moduleDetailsService;

    public UiModuleController(ModuleDetailsService moduleDetailsService) {
        this.moduleDetailsService = moduleDetailsService;
    }

    @GetMapping("/{module}")
    public ModuleDetails getModuleDetails(
            @PathVariable("module") @Parameter(description = "Tai module", required = true)
                    TaiModule module) {
        return moduleDetailsService.getDetails(module);
    }
}
