# Install

```bash
cd tai-stt-python-service
python -m venv .venv
source .venv/Scripts/activate
pip install -r requirements.txt
```

# Run

```bash
uvicorn app.main:app --host 127.0.0.1 --port 8091
```

# Debug endpoints

Dry-run capture without calling the orchestrator:

```bash
curl -X POST "http://localhost:8091/debug/mic/capture"
```

Capture and publish the resulting STT event to the orchestrator:

```bash
curl -X POST "http://localhost:8091/debug/mic/capture-and-callback"
```
