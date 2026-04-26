package com.toteuch.tai.llm.api;

import com.toteuch.tai.llm.api.dto.*;
import com.toteuch.tai.llm.service.LlmGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/llm")
public class LlmGenerationController {
    private final LlmGenerationService service;

    public LlmGenerationController(LlmGenerationService service) {
        this.service = service;
    }

    @PostMapping("/generate-reply")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Start asynchronous LLM generation")
    public LlmGenerationAcceptedResponse generateReply(
            @Valid @RequestBody LlmGenerationRequest request) {
        service.generateReplyAsync(request);
        return new LlmGenerationAcceptedResponse(true, request.correlationId());
    }
}
