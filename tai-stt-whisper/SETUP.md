# Tai STT Whisper Setup

## 1. Requirements

- Python 3.x
- A working C++/native dependency setup for `faster-whisper`
- Internet access for first model download

---

## 2. Create virtual environment

From the `tai-stt-whisper` directory:

```bash
python -m venv .venv
```

Activate it in Git Bash:

```bash
source .venv/Scripts/activate
```

Or PowerShell:

```powershell
.\.venv\Scripts\Activate.ps1
```

Upgrade pip:

```bash
python -m pip install --upgrade pip
```

---

## 3. Install dependencies

```bash
pip install -r requirements.txt
```

---

## 4. Start the service

```bash
uvicorn app.main:app --host 127.0.0.1 --port 8095
```

Swagger UI:

```text
http://localhost:8095/docs
```

Health:

```text
http://localhost:8095/health
```

---

## 5. Test with a local file path

```bash
curl -X POST http://localhost:8095/whisper/transcribe-file \
  -H "Content-Type: application/json" \
  -d '{
    "correlationId": "test-1",
    "audioFile": ".../Tai/tai-stt-listener/input/mic.wav"
  }'
```

---

## 6. Test with upload

```bash
curl -X POST http://localhost:8095/whisper/transcribe-upload \
  -F "correlationId=test-1" \
  -F "file=@.../Tai/tai-stt-listener/input/mic.wav"
```

---

## 7. Notes

The first transcription or startup can be slower because the model may be downloaded and loaded.

The service is intentionally pure:

- no capture
- no gatekeeper
- no orchestrator callback
