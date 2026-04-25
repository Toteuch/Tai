from dataclasses import dataclass


@dataclass(frozen=True)
class SpeechSegment:
    audio_file: str
    duration_ms: int
    average_energy: float
    peak_energy: float
    voiced_ratio: float
    speech_started: bool
    speech_ended: bool

    def to_dict(self) -> dict:
        return {
            "audioFile": self.audio_file,
            "durationMs": self.duration_ms,
            "averageEnergy": self.average_energy,
            "peakEnergy": self.peak_energy,
            "voicedRatio": self.voiced_ratio,
            "speechStarted": self.speech_started,
            "speechEnded": self.speech_ended,
        }
