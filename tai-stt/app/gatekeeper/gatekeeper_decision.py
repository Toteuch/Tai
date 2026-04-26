from dataclasses import dataclass
from enum import Enum


class RejectionCategory(str, Enum):
    NONE = "NONE"
    NOISE = "NOISE"
    UNINTELLIGIBLE = "UNINTELLIGIBLE"


@dataclass(frozen=True)
class GatekeeperDecision:
    accepted: bool
    reason: str
    suspicion_score: int
    rejection_category: RejectionCategory

    def to_dict(self) -> dict:
        return {
            "accepted": self.accepted,
            "reason": self.reason,
            "suspicionScore": self.suspicion_score,
            "rejectionCategory": self.rejection_category.value,
        }
