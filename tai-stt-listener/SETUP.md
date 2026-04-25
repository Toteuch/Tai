# Tai STT Listener Setup

## Requirements

- JDK 21
- Maven
- A working microphone
- Microphone permission enabled for Java / IntelliJ / terminal

---

## Run with Maven

From the module root:

```bash
mvn spring-boot:run
```

---

## Check health

```bash
curl http://localhost:8094/actuator/health
```

---

## Test capture + pre-gatekeeper

```bash
curl -X POST http://localhost:8094/debug/mic/capture
```

Generated WAV files are written to:

```text
input/
```

---

## Calibration tips

If `speechStarted` is always false, lower:

```yaml
tai.stt.capture.silence-threshold
```

If background noise starts speech too easily, raise:

```yaml
tai.stt.capture.silence-threshold
```

If captures stop too late, lower:

```yaml
tai.stt.capture.silence-duration-ms
```

If silence captures take too long, lower:

```yaml
tai.stt.capture.no-speech-timeout-ms
```

If claps/noises pass the pre-gatekeeper too often, raise:

```yaml
tai.stt.gatekeeper.min-voiced-ratio
```
