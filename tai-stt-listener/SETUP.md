# Tai STT Listener Setup

## Requirements

- JDK 21
- Maven
- A working microphone
- A running transcription service reachable through `tai.stt.whisper.base-url`
- A running callback target when callback publication is enabled

---

## Build

From the `tai-stt-listener` module:

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

Start the transcription service before running the full STT pipeline.

Default expected URL:

```text
http://localhost:8095
```

Check transcription service health:

```bash
curl http://localhost:8095/health
```

If callback publication is enabled, start the callback target configured by:

```yaml
tai:
  stt:
    orchestrator:
      base-url: http://localhost:8080
```

---

## Start tai-stt-listener

From the `tai-stt-listener` module:

```bash
mvn spring-boot:run
```

Swagger UI:

```text
http://localhost:8094/docs
```

Health:

```bash
curl http://localhost:8094/actuator/health
```

---

## Continuous listener mode

Start continuous listening:

```bash
curl -X POST "http://localhost:8094/listener/start"
```

Stop continuous listening:

```bash
curl -X POST "http://localhost:8094/listener/stop"
```

Check runtime state through Actuator:

```bash
curl http://localhost:8094/actuator/health
```

Expected flow:

```text
WAITING_FOR_SPEECH
  → CAPTURING
  → PROCESSING
  → WAITING_FOR_SPEECH
```

---

## Debug capture without callbacks

```bash
curl -X POST "http://localhost:8094/debug/mic/capture?correlationId=test-1"
```

Expected flow:

```text
MicCapture
  → PreFiltering
  → Whisper if accepted
  → PostFiltering
  → JSON result
```

---

## Debug capture with final callback

```bash
curl -X POST "http://localhost:8094/debug/mic/capture-and-callback?correlationId=test-1"
```

This endpoint publishes the final STT callback but does not publish `speechStarted`.

---

## Configuration checklist

### Transcription service

```yaml
tai:
  stt:
    whisper:
      base-url: http://localhost:8095
      transcribe-raw-path: /whisper/transcribe-raw
```

### Callback publication

```yaml
tai:
  stt:
    listener:
      publish-speech-started-callbacks: true
      publish-final-callbacks: true
```

Disable callbacks for local listener-only tests:

```yaml
tai:
  stt:
    listener:
      publish-speech-started-callbacks: false
      publish-final-callbacks: false
```

### Accepted languages

```yaml
tai:
  stt:
    gatekeeper:
      allowed-languages:
        - en
```

---

## Tuning capture and gatekeeper

If valid speech is rejected before Whisper, tune:

```yaml
tai.stt.gatekeeper.reject-average-energy-threshold
tai.stt.gatekeeper.min-voiced-ratio
```

If speech segments end too late or too early, tune:

```yaml
tai.stt.capture.silence-threshold
tai.stt.capture.silence-duration-ms
tai.stt.capture.min-recording-ms
tai.stt.capture.max-recording-ms
```

---

## Troubleshooting

### Whisper call fails

Check that the transcription service is running:

```bash
curl http://localhost:8095/health
```

Then verify the listener config:

```yaml
tai:
  stt:
    whisper:
      base-url: http://localhost:8095
      transcribe-raw-path: /whisper/transcribe-raw
```

### Microphone is not detected

Check the listener health endpoint:

```bash
curl http://localhost:8094/actuator/health
```

Look for the `microphoneCapture` component.

### Continuous listener is stopped

Check `continuousListener` in:

```bash
curl http://localhost:8094/actuator/health
```

If needed, restart it:

```bash
curl -X POST "http://localhost:8094/listener/start"
```

### Port already in use

Find the process using port `8094`:

```bash
netstat -ano | findstr :8094
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

## Notes

Debug endpoints are useful for manual validation.

Continuous mode is the normal runtime mode when the listener should keep the microphone open.
