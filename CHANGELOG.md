# Changelog

## 2.0.0 - 2026-05-03

### Added
- Added the first Tai web UI module with a V2 dashboard for live system monitoring and interaction.
- Added orchestrator-backed UI state projection with HTTP and SSE updates.
- Added manual text input and Stop Speak controls from the UI.
- Added on-demand conversation history and module detail views.

### Changed
- Centralized inbound service event contracts across Tai modules.
- Updated the orchestrator to expose live module runtime state for the UI.
- Improved module health refresh and UI state synchronization.
- Updated documentation for the V2 UI runtime and module architecture.

### Removed
- Removed duplicated inbound event definitions from individual modules.
- Removed the old duplicated transport event ownership layer.

## 1.0.0 - 2026-04-29

### Added
- Event-driven orchestrator
- Continuous STT listener
- GPU-backed Whisper transcription
- Piper TTS playback
- Ollama LLM service integration
- Barge-in support
- Turn performance metrics
- System health aggregation
