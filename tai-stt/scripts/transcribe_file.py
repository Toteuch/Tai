from faster_whisper import WhisperModel
import sys
import json

if len(sys.argv) < 2:
    print(json.dumps({"success": False, "error": "Missing audio file path"}))
    sys.exit(1)

audio_path = sys.argv[1]
model_size = sys.argv[2] if len(sys.argv) > 2 else "small"
device = sys.argv[3] if len(sys.argv) > 3 else "cpu"
compute_type = sys.argv[4] if len(sys.argv) > 4 else "int8"

model = WhisperModel(model_size, device=device, compute_type=compute_type)

segments, info = model.transcribe(audio_path)

text = " ".join(segment.text.strip() for segment in segments).strip()

print(json.dumps({
    "success": True,
    "text": text,
    "language": info.language,
    "language_probability": info.language_probability
}))
