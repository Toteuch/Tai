package com.toteuch.tai.orchestrator.support;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class SystemPromptLoader {

    private final String systemPrompt;

    public SystemPromptLoader(@Value("${tai.prompt.system-file}") Resource resource)
            throws IOException {
        try (InputStream inputStream = resource.getInputStream()) {
            this.systemPrompt =
                    new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
        }
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }
}
