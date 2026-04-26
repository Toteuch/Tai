package com.toteuch.tai.llm.ollama.dto;

import java.util.List;

public record OllamaChatRequest(String model, List<OllamaMessage> messages, boolean stream, String keep_alive) {
}
