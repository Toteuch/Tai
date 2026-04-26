# Tai TTS Piper Setup

This document explains how to prepare and run the `tai-tts-piper` microservice locally.

The service expects all Piper-related resources to live inside the module:

```text
tai-tts-piper/
  .venv/
  voices/
  output/
  src/
  pom.xml
```

---

## 1. Requirements

- JDK 21
- Maven
- Python available from your terminal
- Internet access for the first setup
- Speakers / audio output available on the machine

---

## 2. Create the Python virtual environment

From the `tai-tts-piper` directory:

```bash
python -m venv .venv
```

Activate it in Git Bash:

```bash
source .venv/Scripts/activate
```

Or in PowerShell:

```powershell
.\.venv\Scripts\Activate.ps1
```

Upgrade pip:

```bash
python -m pip install --upgrade pip
```

---

## 3. Install Piper

```bash
pip install piper-tts
```

After installation, the executable should be available at:

```text
.venv/Scripts/piper.exe
```

Check it:

```bash
./.venv/Scripts/piper.exe --help
```

---

## 4. Create runtime folders

```bash
mkdir -p voices output
```

---

## 5. Download the default voice

Default voice used by the module:

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

## 6. Check configuration

Default configuration:

```yaml
tai:
  tts:
    piper:
      executable: ./.venv/Scripts/piper.exe
      model: ./voices/en_GB-alba-medium.onnx
      config: ./voices/en_GB-alba-medium.onnx.json
      output-dir: ./output
      voice-id: en_GB-alba-medium
```

If you use another voice, update:

- `model`
- `config`
- `voice-id`

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

```text
http://localhost:8093/actuator/health
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

---

## 9. Stop playback manually

```bash
curl -X POST http://localhost:8093/tts/stop \
  -H "Content-Type: application/json" \
  -d '{"correlationId":"test-1"}'
```

---

## 10. Typical local run order with Tai

```text
1. Start tai-stt
2. Trigger a STT capture
3. Start tai-orchestrator
4. Start tai-llm
5. Start tai-tts-piper
```
