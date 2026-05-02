#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [ ! -f "$SCRIPT_DIR/.venv/Scripts/activate" ]; then
  echo "Missing virtual environment. Expected: $SCRIPT_DIR/.venv/Scripts/activate"
  exit 1
fi

source "$SCRIPT_DIR/.venv/Scripts/activate"

export PATH="$SCRIPT_DIR/.venv/Lib/site-packages/nvidia/cublas/bin:$SCRIPT_DIR/.venv/Lib/site-packages/nvidia/cudnn/bin:$PATH"

uvicorn app.main:app --host 127.0.0.1 --port 8095
