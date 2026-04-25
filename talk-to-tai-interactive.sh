#!/usr/bin/env bash

set -euo pipefail

SESSION_ID="${SESSION_ID:-test}"
BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "Talking to Tai with sessionId=${SESSION_ID}"
echo "Type /quit to exit"
echo

while true; do
  read -r -p "> " TEXT

  if [ "${TEXT}" = "/quit" ]; then
    break
  fi

  curl -s -X POST -G "${BASE_URL}/debug/text" \
    --data-urlencode "sessionId=${SESSION_ID}" \
    --data-urlencode "text=${TEXT}"

  echo
  echo
done
