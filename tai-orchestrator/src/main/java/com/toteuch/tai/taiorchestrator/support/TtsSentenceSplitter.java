package com.toteuch.tai.taiorchestrator.support;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TtsSentenceSplitter {

    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[^.!?]+[.!?]?");

    public List<String> split(String text) {
        List<String> segments = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return segments;
        }

        Matcher matcher = SENTENCE_PATTERN.matcher(text);
        while (matcher.find()) {
            String segment = matcher.group().trim();
            if (!segment.isBlank()) {
                segments.add(segment);
            }
        }

        return segments;
    }
}
