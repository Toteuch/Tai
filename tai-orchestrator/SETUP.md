# Tai Orchestrator Setup

## Requirements

- JDK 21
- Maven
- `tai-llm` reachable from the orchestrator
- `tai-tts-piper` reachable from the orchestrator when TTS is enabled
- STT callbacks sent to the orchestrator by `tai-stt-listener`

---

## Build

From the `tai-orchestrator` module:

```bash
mvn clean compile
```

Run tests:

```bash
mvn test
```

Package:

```bash
mvn clean package
```

---

## Start dependencies

Start the services used by the orchestrator before running a full voice flow.

Expected local services:

```text
tai-llm        http://localhost:<configured-llm-port>
tai-tts-piper  http://localhost:<configured-tts-port>
```

The STT listener calls the orchestrator callback endpoints, so the orchestrator must be running before testing the full voice loop.

---

## Start tai-orchestrator

From the `tai-orchestrator` module:

```bash
mvn spring-boot:run
```

Default local URL:

```text
http://localhost:8080
```

---

## Health

If Actuator is enabled, check health with:

```bash
curl http://localhost:8080/actuator/health
```

---

## Callback endpoint smoke tests

### STT speech started

```bash
curl -X POST "http://localhost:8080/events/stt/speech-started" \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "test-event-1",
    "createdAt": "2026-04-27T18:40:15.123Z",
    "source": "STT_SERVICE",
    "correlationId": "test-correlation-1",
    "averageEnergy": 132.22,
    "peakEnergy": 643.66
  }'
```

### STT transcript accepted

```bash
curl -X POST "http://localhost:8080/events/stt/transcript-accepted" \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "test-event-2",
    "createdAt": "2026-04-27T18:40:16.123Z",
    "source": "STT_SERVICE",
    "correlationId": "test-correlation-1",
    "text": "Hello Tai",
    "language": "en",
    "languageProbability": 0.98,
    "durationMs": 2500,
    "averageEnergy": 120.5,
    "reason": "ACCEPTED",
    "suspicionScore": 0,
    "transcriptionDurationMs": 390
  }'
```

Use real service flows for LLM and TTS callback testing, because those callbacks usually need an active turn with a matching correlation id.

---

## Full local voice flow

Recommended local order:

```text
1. Start tai-llm
2. Start tai-tts-piper
3. Start tai-orchestrator
4. Start tai-stt-whisper
5. Start tai-stt-listener
6. Start continuous listening on tai-stt-listener (if not already started)
```

Start continuous listening:

```bash
curl -X POST "http://localhost:8094/listener/start"
```

Stop continuous listening:

```bash
curl -X POST "http://localhost:8094/listener/stop"
```

---

## Logs

The orchestrator writes logs to the configured log directory.

Typical local property:

```yaml
tai:
  logs:
    dir: ./logs
```

Expected log categories:

```text
conversation log
performance log
application log
error log
decision log
context log
trace log
```

Performance logs contain one consolidated line per completed turn on INFO level:

```text
TURN metrics | correlationId=... totalTurnMs=... startedFrom=... transcriptDurationMs=... speechToTranscriptMs=... llmGenerationMs=... ttsSynthesisMs=... assistantFirstAudioLatencyMs=... ttsSpeechDurationMs=...
```

### Note

Conversation log on DEBUG level will also log dependencies performances on their own lines.

---

## Configuration checklist

### LLM client

Check the LLM service base URL and endpoints in application configuration.

The orchestrator sends LLM generation requests through its configured `LlmClient`.

### TTS client

Check the TTS service base URL and endpoints in application configuration.

The orchestrator sends speech and stop commands through its configured `TtsClient`.

### Logs

Set the log directory explicitly:

```yaml
tai:
  logs:
    dir: ./logs
```

### Spring profiles for tests

Tests can use a dedicated Spring profile to avoid writing runtime logs into the normal local log directory.

---

## Troubleshooting

### Port 8080 already in use

Find the process using port `8080`:

```bash
netstat -ano | findstr :8080
```

Kill it from Git Bash:

```bash
taskkill //PID <PID> //F
```

Or from Windows CMD:

```cmd
taskkill /PID <PID> /F
```

---

### STT callbacks do not reach the orchestrator

Check that the orchestrator is running:

```bash
curl http://localhost:8080/actuator/health
```

Then check the STT listener callback configuration:

```yaml
tai:
  stt:
    orchestrator:
      base-url: http://localhost:8080
```

---

### LLM response is ignored

A late LLM response can be ignored when its correlation id no longer matches the active turn.

Check conversation logs and performance logs for the same `correlationId`.

---

### No TTS playback

Check:

- TTS enabled flag in orchestrator configuration
- TTS service health
- TTS client base URL
- `TtsPlaybackStartedEvent`
- `TtsPlaybackFailedEvent`

---

## Notes

The orchestrator should be started before running the full listener-based voice flow because STT callbacks target orchestrator endpoints.

Manual callback curl commands are useful for smoke tests, but realistic behavior is best validated through the full service flow.
