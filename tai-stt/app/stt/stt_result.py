from dataclasses import dataclass
from typing import Optional


@dataclass(frozen=True)
class SttResult:
    success: bool
    text: Optional[str]
    language: Optional[str]
    language_probability: Optional[float]
    error_code: Optional[str] = None
    error_message: Optional[str] = None

    def to_dict(self) -> dict:
        return {
            "success": self.success,
            "text": self.text,
            "language": self.language,
            "languageProbability": self.language_probability,
            "errorCode": self.error_code,
            "errorMessage": self.error_message,
        }
