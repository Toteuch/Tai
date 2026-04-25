package com.toteuch.tai.taiorchestrator.support;

import org.springframework.stereotype.Component;

@Component
public class TtsTextSanitizer {

    public String sanitize(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }

        String sanitized = text;

        // Remove roleplay / stage directions between asterisks
        sanitized = sanitized.replaceAll("\\*[^*]{1,80}\\*", " ");

        // Remove repeated whitespace
        sanitized = sanitized.replaceAll("\\s+", " ");

        // Remove extra spaces before punctuation
        sanitized = sanitized.replaceAll("\\s+([,.;!?])", "$1");

        // Normalize repeated punctuation
        sanitized = sanitized.replaceAll("!{2,}", "!");
        sanitized = sanitized.replaceAll("\\?{2,}", "?");
        sanitized = sanitized.replaceAll("\\.{4,}", "...");

        // Clean orphan punctuation caused by removed stage directions
        sanitized = sanitized.replaceAll("(^|\\s)[,.;:!?](\\s|$)", " ");

        // Final whitespace cleanup
        sanitized = sanitized.replaceAll("\\s+", " ").trim();

        return sanitized;
    }
}
