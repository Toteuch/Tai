package com.toteuch.tai.taiorchestrator.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TtsTextSanitizerTest {

    private final TtsTextSanitizer sanitizer = new TtsTextSanitizer();

    @Test
    void shouldRemoveStageDirectionsBetweenAsterisks() {
        String input = "*laughs* Hey, Toteuch!";
        String output = sanitizer.sanitize(input);

        assertEquals("Hey, Toteuch!", output);
    }

    @Test
    void shouldRemoveMultipleStageDirections() {
        String input = "Well... *laughs* I don't know. *sighs* Maybe.";
        String output = sanitizer.sanitize(input);

        assertEquals("Well... I don't know. Maybe.", output);
    }

    @Test
    void shouldNormalizeWhitespaceAndPunctuation() {
        String input = "Hello   ,   Toteuch!!!";
        String output = sanitizer.sanitize(input);

        assertEquals("Hello, Toteuch!", output);
    }

    @Test
    void shouldKeepNormalTextUntouchedWhenNoCleanupIsNeeded() {
        String input = "RAM is temporary memory.";
        String output = sanitizer.sanitize(input);

        assertEquals("RAM is temporary memory.", output);
    }
}
