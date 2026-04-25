package com.toteuch.tai.taiorchestrator.support;

import org.springframework.stereotype.Component;

@Component
public class TtsTextPreprocessor {

    public String preprocess(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }

        String processed = text;

        // Normalize curly apostrophes and quotes
        processed = processed.replace("’", "'");
        processed = processed.replace("“", "\"");
        processed = processed.replace("”", "\"");

        // Make punctuation a bit less aggressive for Piper
        processed = processed.replace('!', '.');

        // Replace dash-style pauses with sentence boundaries
        processed = processed.replace(" - ", ". ");
        processed = processed.replace(" – ", ". ");
        processed = processed.replace(" — ", ". ");

        // Reduce repeated punctuation
        processed = processed.replaceAll("\\.{2,}", ".");
        processed = processed.replaceAll("\\?{2,}", "?");

        // Collapse whitespace
        processed = processed.replaceAll("\\s+", " ").trim();

        return processed;
    }
}
