# Tai LLM Microservice

## Overview

The **Tai LLM microservice** generates assistant replies through Ollama.

It is decoupled from the orchestrator:

```text
Tai Orchestrator → HTTP LlmClient → Tai LLM microservice → Ollama → callback to Orchestrator
```

The service accepts generation requests asynchronously and sends the result back through orchestrator callbacks.

---

## Architecture

```text
LlmGenerationController
  ↓
LlmGenerationService (@Async)
  ↓
OllamaClient
  ↓
Ollama /api/chat
  ↓
OrchestratorLlmEventClient
  ↓
POST /events/llm/*
```

| Component                    | Role                                           |
|------------------------------|------------------------------------------------|
| `LlmGenerationController`    | HTTP API called by the orchestrator LLM client |
| `LlmGenerationService`       | Async generation orchestration                 |
| `OllamaClient`               | Calls Ollama `/api/chat`                       |
| `OrchestratorLlmEventClient` | Sends callbacks to the orchestrator            |
| `ModelWarmupService`         | Warms the Ollama model at startup              |
| `ModelWarmupState`           | Stores model warmup status                     |
| `OllamaHealthIndicator`      | Checks Ollama availability                     |
| `ModelWarmupHealthIndicator` | Reports model warmup status                    |

---

## Exposed controller

### `POST /llm/generate-reply`

Starts asynchronous LLM generation.

Request:

```json
{
    "correlationId": "test-1",
    "messages": [
        {
            "role": "system",
            "content": "You are Tai."
        },
        {
            "role": "user",
            "content": "Hello Tai"
        }
    ]
}
```

Response:

```json
{
    "accepted": true,
    "correlationId": "test-1"
}
```

This only confirms that the request was accepted. The answer is sent later through callback.

---

## Swagger UI

```text
http://localhost:8092/docs
```

OpenAPI JSON:

```text
http://localhost:8092/v3/api-docs
```

---

## Generated events

### `LLM_RESPONSE_COMPLETED`

Callback endpoint:

```http
POST /events/llm/response-completed
```

Payload:

```json
{
    "eventId": "...",
    "createdAt": "2026-04-26T12:00:00Z",
    "source": "LLM_SERVICE",
    "correlationId": "test-1",
    "responseText": "Hi!",
    "modelName": "tai-llama",
    "inputTokens": 10,
    "outputTokens": 20,
    "generationDurationMs": 512
}
```

### `LLM_RESPONSE_FAILED`

Callback endpoint:

```http
POST /events/llm/response-failed
```

Payload:

```json
{
    "eventId": "...",
    "createdAt": "2026-04-26T12:00:00Z",
    "source": "LLM_SERVICE",
    "correlationId": "test-1",
    "errorCode": "OLLAMA_EMPTY_MESSAGE",
    "errorMessage": "Ollama returned no assistant message content."
}
```

---

## Business flows

### Nominal generation

```text
1. Orchestrator builds LLM context
2. Orchestrator LlmClient calls POST /llm/generate-reply
3. LLM service accepts the request immediately
4. LLM service generates asynchronously through Ollama
5. Ollama returns an assistant message
6. LLM service calls /events/llm/response-completed
7. Orchestrator resumes the conversation flow
```

### Failed generation

```text
1. Orchestrator calls POST /llm/generate-reply
2. LLM service starts async generation
3. Ollama fails, times out, or returns an invalid response
4. LLM service maps the failure to errorCode/errorMessage
5. LLM service calls /events/llm/response-failed
6. Orchestrator handles the assistant reply failure
```

### Startup warmup

```text
1. LLM service starts
2. ModelWarmupService sends a minimal chat request to Ollama
3. Request includes keep_alive
4. If successful, the model is considered warm
5. Later requests reuse the loaded model while Ollama keeps it alive
```

---

## Properties

```yaml
server:
    port: 8092

tai:
    llm:
        async:
            core-pool-size: 1
            max-pool-size: 2
            queue-capacity: 50
        orchestrator:
            base-url: http://localhost:8080
            connect-timeout-ms: 3000
            read-timeout-ms: 10000
            callbacks:
                response-completed-path: /events/llm/response-completed
                response-failed-path: /events/llm/response-failed
        ollama:
            base-url: http://localhost:11434
            chat-path: /api/chat
            tags-path: /api/tags
            model: tai-llama
            stream: false
            keep-alive: 24h
            warm-up-on-startup: true
            warm-up-prompt: "Say ready."
            connect-timeout-ms: 3000
            read-timeout-ms: 120000
```

| Property                                                 | Description                                              |
|----------------------------------------------------------|----------------------------------------------------------|
| `server.port`                                            | HTTP port of the LLM microservice                        |
| `tai.llm.async.core-pool-size`                           | Minimum async worker threads                             |
| `tai.llm.async.max-pool-size`                            | Maximum async worker threads                             |
| `tai.llm.async.queue-capacity`                           | Pending generation request queue size                    |
| `tai.llm.orchestrator.base-url`                          | Orchestrator base URL                                    |
| `tai.llm.orchestrator.connect-timeout-ms`                | Callback connection timeout                              |
| `tai.llm.orchestrator.read-timeout-ms`                   | Callback read timeout                                    |
| `tai.llm.orchestrator.callbacks.response-completed-path` | Success callback path                                    |
| `tai.llm.orchestrator.callbacks.response-failed-path`    | Failure callback path                                    |
| `tai.llm.ollama.base-url`                                | Ollama base URL                                          |
| `tai.llm.ollama.chat-path`                               | Ollama chat path                                         |
| `tai.llm.ollama.tags-path`                               | Ollama tags path used by health                          |
| `tai.llm.ollama.model`                                   | Ollama model name                                        |
| `tai.llm.ollama.stream`                                  | Whether Ollama streams tokens; should stay false for now |
| `tai.llm.ollama.keep-alive`                              | How long Ollama keeps the model loaded                   |
| `tai.llm.ollama.warm-up-on-startup`                      | Whether to warm the model at startup                     |
| `tai.llm.ollama.warm-up-prompt`                          | Prompt used for startup warmup                           |
| `tai.llm.ollama.connect-timeout-ms`                      | Ollama connection timeout                                |
| `tai.llm.ollama.read-timeout-ms`                         | Ollama read timeout                                      |

---

## Health

Spring Boot Actuator is enabled.

### Main endpoint

```http
GET /actuator/health
```

Example:

```json
{
    "status": "UP",
    "components": {
        "ollama": {
            "status": "UP",
            "details": {
                "baseUrl": "http://localhost:11434",
                "model": "tai-llama"
            }
        },
        "modelWarmup": {
            "status": "UP",
            "details": {
                "model": "tai-llama",
                "warm": true,
                "lastWarmupAt": "2026-04-26T12:00:00Z",
                "lastError": null
            }
        }
    }
}
```

| Component     | Meaning                                  |
|---------------|------------------------------------------|
| `ollama`      | Checks whether Ollama is reachable       |
| `modelWarmup` | Reports whether startup warmup succeeded |

| Status     | Meaning                                         |
|------------|-------------------------------------------------|
| `UP`       | Service and checked dependencies are healthy    |
| `DOWN`     | Required dependency unavailable                 |
| `DEGRADED` | Service runs but model warmup has not succeeded |

---

## Running locally

```bash
mvn spring-boot:run
```

Open:

```text
http://localhost:8092/docs
```

Test generation:

```bash
curl -X POST http://localhost:8092/llm/generate-reply \
  -H "Content-Type: application/json" \
  -d '{
    "correlationId": "test-1",
    "messages": [
      { "role": "user", "content": "Hello Tai" }
    ]
  }'
```
