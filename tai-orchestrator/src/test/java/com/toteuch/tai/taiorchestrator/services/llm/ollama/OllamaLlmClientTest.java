package com.toteuch.tai.taiorchestrator.services.llm.ollama;

import com.toteuch.tai.taiorchestrator.services.llm.LlmGenerationResult;
import com.toteuch.tai.taiorchestrator.services.llm.LlmMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OllamaLlmClientTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;
    private OllamaProperties ollamaProperties;
    private OllamaLlmClient ollamaLlmClient;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);

        ollamaProperties = new OllamaProperties();
        ollamaProperties.setBaseUrl("http://localhost:11434");
        ollamaProperties.setChatPath("/api/chat");
        ollamaProperties.setModel("tai-llama");
        ollamaProperties.setStream(false);
        ollamaProperties.setKeepAlive("30m");

        ollamaLlmClient = new OllamaLlmClient(restTemplate, ollamaProperties);
    }

    @Test
    void shouldReturnSuccessfulGenerationResultWhenOllamaReturnsValidResponse() {
        String responseBody = """
            {
              "model": "tai-llama",
              "created_at": "2026-04-21T20:00:00Z",
              "message": {
                "role": "assistant",
                "content": "Hey Toteuch. I am here."
              },
              "done": true,
              "total_duration": 123456789,
              "load_duration": 123456,
              "prompt_eval_count": 42,
              "prompt_eval_duration": 1000000,
              "eval_count": 17,
              "eval_duration": 2000000
            }
            """;

        mockServer.expect(requestTo("http://localhost:11434/api/chat"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.model").value("tai-llama"))
            .andExpect(jsonPath("$.stream").value(false))
            .andExpect(jsonPath("$.keepAlive").value("30m"))
            .andExpect(jsonPath("$.messages[0].role").value("system"))
            .andExpect(jsonPath("$.messages[0].content").value("You are Tai."))
            .andExpect(jsonPath("$.messages[1].role").value("user"))
            .andExpect(jsonPath("$.messages[1].content").value("Hello"))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        List<LlmMessage> messages = List.of(
            new LlmMessage("system", "You are Tai."),
            new LlmMessage("user", "Hello")
        );

        LlmGenerationResult result = ollamaLlmClient.generateReply("corr-1", messages);

        assertTrue(result.success());
        assertEquals("Hey Toteuch. I am here.", result.responseText());
        assertEquals("tai-llama", result.modelName());
        assertEquals(42, result.inputTokens());
        assertEquals(17, result.outputTokens());
        assertNotNull(result.generationDurationMs());
        assertNull(result.errorCode());
        assertNull(result.errorMessage());

        mockServer.verify();
    }

    @Test
    void shouldReturnFailureWhenResponseBodyIsEmpty() {
        mockServer.expect(requestTo("http://localhost:11434/api/chat"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        List<LlmMessage> messages = List.of(
            new LlmMessage("system", "You are Tai."),
            new LlmMessage("user", "Hello")
        );

        LlmGenerationResult result = ollamaLlmClient.generateReply("corr-1", messages);

        assertFalse(result.success());
        assertEquals("OLLAMA_EMPTY_RESPONSE", result.errorCode());

        mockServer.verify();
    }

    @Test
    void shouldReturnFailureWhenAssistantMessageContentIsMissing() {
        String responseBody = """
            {
              "model": "tai-llama",
              "message": {
                "role": "assistant",
                "content": ""
              },
              "done": true
            }
            """;

        mockServer.expect(requestTo("http://localhost:11434/api/chat"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        List<LlmMessage> messages = List.of(
            new LlmMessage("system", "You are Tai."),
            new LlmMessage("user", "Hello")
        );

        LlmGenerationResult result = ollamaLlmClient.generateReply("corr-1", messages);

        assertFalse(result.success());
        assertEquals("OLLAMA_EMPTY_MESSAGE", result.errorCode());

        mockServer.verify();
    }

    @Test
    void shouldReturnFailureWhenOllamaReturnsHttpError() {
        mockServer.expect(requestTo("http://localhost:11434/api/chat"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\":\"internal error\"}"));

        List<LlmMessage> messages = List.of(
            new LlmMessage("system", "You are Tai."),
            new LlmMessage("user", "Hello")
        );

        LlmGenerationResult result = ollamaLlmClient.generateReply("corr-1", messages);

        assertFalse(result.success());
        assertEquals("OLLAMA_HTTP_ERROR", result.errorCode());

        mockServer.verify();
    }

    @Test
    void shouldReturnFailureWhenConnectionFails() {
        mockServer.expect(requestTo("http://localhost:11434/api/chat"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(request -> {
                throw new ResourceAccessException("Connection refused");
            });

        List<LlmMessage> messages = List.of(
            new LlmMessage("system", "You are Tai."),
            new LlmMessage("user", "Hello")
        );

        LlmGenerationResult result = ollamaLlmClient.generateReply("corr-1", messages);

        assertFalse(result.success());
        assertEquals("OLLAMA_CONNECTION_ERROR", result.errorCode());
        assertTrue(result.retryable());

        mockServer.verify();
    }
}
