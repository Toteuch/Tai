# Tai TTS Piper Setup

## Requirements

- JDK 21
- Maven
- Python available from the terminal
- Internet access for the first setup
- Speakers or audio output available on the machine

---

## 1. Create the Python virtual environment

From the `tai-tts-piper` directory:

```bash
python -m venv .venv
```

Activate it in Git Bash:

```bash
source .venv/Scripts/activate
```

Upgrade pip:

```bash
python -m pip install --upgrade pip
```

---

## 2. Install Piper

```bash
python -m pip install piper-tts
```

Check the executable:

```bash
./.venv/Scripts/piper.exe --help
```

Expected executable path:

```text
.venv/Scripts/piper.exe
```

---

## 3. Create runtime folders

```bash
mkdir -p voices output
```

---

## 4. Download the configured voice

Default voice:

```text
en_GB-alba-medium
```

Download the ONNX model:

```bash
curl -L -o voices/en_GB-alba-medium.onnx \
  https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_GB/alba/medium/en_GB-alba-medium.onnx
```

Download the JSON config:

```bash
curl -L -o voices/en_GB-alba-medium.onnx.json \
  https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_GB/alba/medium/en_GB-alba-medium.onnx.json
```

Expected files:

```text
voices/
  en_GB-alba-medium.onnx
  en_GB-alba-medium.onnx.json
```

---

## 5. Check configuration

Default Piper configuration:

```yaml
tai:
  tts:
    piper:
      executable: ./.venv/Scripts/piper.exe
      model: ./voices/en_GB-alba-medium.onnx
      config: ./voices/en_GB-alba-medium.onnx.json
      output-dir: ./output
      voice-id: en_GB-alba-medium
      process-timeout-ms: 60000
```

If another voice is used, update:

- `tai.tts.piper.model`
- `tai.tts.piper.config`
- `tai.tts.piper.voice-id`

---

## 6. Build

From the `tai-tts-piper` module:

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

## 7. Start the service

```bash
mvn spring-boot:run
```

Swagger UI:

```text
http://localhost:8093/docs
```

Health endpoint:

```bash
curl http://localhost:8093/actuator/health
```

---

## 8. Test speech manually

```bash
curl -X POST http://localhost:8093/tts/speak \
  -H "Content-Type: application/json" \
  -d '{"correlationId":"test-1","text":"Hello, I am Tai."}'
```

Generated WAV files are written to:

```text
output/
```

Played WAV files are deleted after playback.

---

## 9. Stop playback manually

```bash
curl -X POST http://localhost:8093/tts/stop \
  -H "Content-Type: application/json" \
  -d '{"correlationId":"test-1"}'
```

---

## 10. Callback target

When callback publication is enabled, the configured callback target must be running.

Default callback base URL:

```yaml
tai:
  tts:
    orchestrator:
      base-url: http://localhost:8080
```

Callback paths:

```yaml
tai:
  tts:
    orchestrator:
      callbacks:
        playback-started-path: /events/tts/playback-started
        playback-completed-path: /events/tts/playback-completed
        playback-failed-path: /events/tts/playback-failed
```

---

## 11. Troubleshooting

### Piper executable is missing

Check:

```bash
./.venv/Scripts/piper.exe --help
```

If the command fails, activate the virtual environment and reinstall Piper:

```bash
source .venv/Scripts/activate
python -m pip install piper-tts
```

---

### Voice files are missing

Check:

```bash
ls voices
```

Expected files:

```text
en_GB-alba-medium.onnx
en_GB-alba-medium.onnx.json
```

---

### No audio output

Check that the WAV file is generated in `output/`.

If the WAV exists and contains speech, the issue is in local playback or audio output configuration.

---

### Port already in use

Find the process using port `8093`:

```bash
netstat -ano | findstr :8093
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

The service expects Piper resources to stay inside the module:

```text
.venv/
voices/
output/
```

This keeps the module self-contained and avoids depending on an external Piper installation.
