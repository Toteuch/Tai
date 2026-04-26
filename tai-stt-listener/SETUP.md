# Tai STT Listener Setup

## Requirements

- JDK 21
- Maven
- A working microphone
- `tai-stt-whisper` running on port `8095`

---

## Run order

```text
1. Start tai-stt-whisper
2. Start tai-stt-listener
3. Call /debug/mic/capture
```

---

## Start tai-stt-whisper

From the `tai-stt-whisper` module:

```bash
source .venv/Scripts/activate
uvicorn app.main:app --host 127.0.0.1 --port 8095
```

Check:

```bash
curl http://localhost:8095/health
```

---

## Start tai-stt-listener

From the `tai-stt-listener` module:

```bash
mvn spring-boot:run
```

Swagger:

```text
http://localhost:8094/docs
```

---

## Test full debug pipeline

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

## Debug notes

If `preGatekeeperDecision` is not null, Whisper was intentionally skipped.

If `transcription.errorCode = WHISPER_HTTP_ERROR`, verify that `tai-stt-whisper` is running.

If valid short utterances are rejected before Whisper, tune:

```yaml
tai.stt.gatekeeper.reject-average-energy-threshold
tai.stt.gatekeeper.min-voiced-ratio
```
