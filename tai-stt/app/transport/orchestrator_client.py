from __future__ import annotations

from dataclasses import dataclass
from typing import Optional

import requests

from app.config import OrchestratorSettings
from app.transport.stt_events import PreparedSttEvent


@dataclass(frozen=True)
class CallbackResult:
    attempted: bool
    success: bool
    url: Optional[str]
    status_code: Optional[int]
    error: Optional[str]

    def to_dict(self) -> dict:
        return {
            "attempted": self.attempted,
            "success": self.success,
            "url": self.url,
            "statusCode": self.status_code,
            "error": self.error,
        }


class OrchestratorClient:
    def __init__(self, settings: OrchestratorSettings):
        self.settings = settings

    def send_stt_event(self, event: PreparedSttEvent) -> CallbackResult:
        path = self.settings.callbacks[event.callback_name]
        url = self.settings.base_url.rstrip("/") + path

        try:
            response = requests.post(url, json=event.payload, timeout=10)
            return CallbackResult(
                attempted=True,
                success=200 <= response.status_code < 300,
                url=url,
                status_code=response.status_code,
                error=None if 200 <= response.status_code < 300 else response.text,
            )
        except Exception as exc:
            return CallbackResult(True, False, url, None, str(exc))
