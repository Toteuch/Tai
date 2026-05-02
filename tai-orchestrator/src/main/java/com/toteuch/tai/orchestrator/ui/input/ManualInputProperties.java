// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.input;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tai.ui.manual-input")
public class ManualInputProperties {

    private int maxLength = 2000;

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
}
