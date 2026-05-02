# Tai STT Whisper Setup

## 1. Requirements

- Python 3.x
- NVIDIA GPU visible from Windows
- NVIDIA driver installed
- Internet access for first model download

Check that the GPU is visible:

```bash
nvidia-smi
```

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

Upgrade pip:

```bash
python -m pip install --upgrade pip
```

---

## 3. Install dependencies

```bash
python -m pip install --upgrade -r requirements.txt
```

The GPU runtime requires the NVIDIA packages declared in `requirements.txt`:

```text
nvidia-cublas-cu12
nvidia-cudnn-cu12
```

---

## 4. Configure `config.yaml`

Current GPU configuration:

```yaml
server:
  host: 127.0.0.1
  port: 8095

whisper:
  model-size: small
  device: cuda
  compute-type: int8_float16
  beam-size: 5
  temperature: 0.0
  condition-on-previous-text: false
  vad-filter: false
  language: en
  initial-prompt: "The assistant is named Tai. Expected language is English. Common words: Tai, LLM, TTS, STT."

storage:
  temp-dir: ./tmp
```

---

## 5. Make NVIDIA DLLs visible to Python

The NVIDIA packages install DLLs inside `.venv`, but those folders must be present in the process `PATH`.

### Git Bash

```bash
export PATH="$PWD/.venv/Lib/site-packages/nvidia/cublas/bin:$PWD/.venv/Lib/site-packages/nvidia/cudnn/bin:$PATH"
```

---

## 6. Verify CUDA loading

Run this from the same shell where the `PATH` was updated:

```bash
python -c "from faster_whisper import WhisperModel; WhisperModel('small', device='cuda', compute_type='int8_float16'); print('CUDA OK')"
```

Expected output:

```text
CUDA OK
```

---

## 7. Start the service

Use the project launch script.

### Git Bash

```bash
./run-gpu.sh
```

The launch script must:

1. activate `.venv`
2. add NVIDIA DLL folders to `PATH`
3. start Uvicorn on port `8095`

---

## 8. Check health

```bash
curl http://localhost:8095/health
```

Expected response:

```json
{
  "status": "UP",
  "modelLoaded": true,
  "modelSize": "small",
  "device": "cuda",
  "computeType": "int8_float16",
  "lastError": null
}
```

---

## 9. Open Swagger UI

```text
http://localhost:8095/docs
```

---

## 10. Test raw transcription endpoint

```bash
curl -X POST http://localhost:8095/whisper/transcribe-raw \
  -H "Content-Type: audio/wav" \
  -H "X-Correlation-Id: test-raw-1" \
  -H "X-Filename: mic.wav" \
  --data-binary "@./input/mic.wav"
```

---

## 11. Test upload endpoint

```bash
curl -X POST http://localhost:8095/whisper/transcribe-upload \
  -F "correlationId=test-upload-1" \
  -F "file=@./input/mic.wav;type=audio/wav"
```

---

## 12. Troubleshooting

### `cublas64_12.dll is not found or cannot be loaded`

The Python process cannot find NVIDIA CUDA/cuBLAS DLLs.

Check that the shell used to start the service includes:

```text
.venv/Lib/site-packages/nvidia/cublas/bin
.venv/Lib/site-packages/nvidia/cudnn/bin
```

Then restart the service.

---

### `where cublas64_12.dll` finds nothing

This only checks the global Windows `PATH`.

When using pip-installed NVIDIA packages, this is expected unless the venv NVIDIA folders were added to `PATH` for the current process.

---

### GPU works from a terminal but not from the IDE

The `PATH` update is process-local.

Either start the service from the terminal where `PATH` was updated, or add the NVIDIA DLL folders to the IDE run configuration environment.

---

### Stop a stuck service process

Find the process using port `8095`:

```bash
netstat -ano | findstr :8095
```

Kill it from Git Bash:

```bash
taskkill //PID <PID> //F
```

Or from CMD:

```cmd
taskkill /PID <PID> /F
```

---

## 13. Notes

The first startup can be slower because the Whisper model may need to be downloaded and loaded.

The service is intentionally pure:

- no microphone capture
- no gatekeeper
- no orchestrator callback
- no conversation state
