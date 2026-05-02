// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.health;

import com.toteuch.tai.orchestrator.ui.model.TaiModule;

public interface ModuleHealthClient {

    ModuleHealthResult check(TaiModule module);
}
