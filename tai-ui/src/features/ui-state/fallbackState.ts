// SPDX-License-Identifier: GPL-3.0-only
import { TaiUiState } from '@/features/ui-state/types';

export const fallbackTaiUiState: TaiUiState = {
  schemaVersion: '2.0',
  sequence: 0,
  generatedAt: new Date().toISOString(),
  conversationStatus: 'LISTENING',
  modules: {
    SYSTEM: {
      module: 'SYSTEM',
      health: 'UP',
      activity: 'IDLE',
      displayState: 'Healthy',
      stale: false,
    },
    ORCHESTRATOR: {
      module: 'ORCHESTRATOR',
      health: 'UP',
      activity: 'IDLE',
      displayState: 'Active',
      stale: false,
    },
    STT_LISTENER: {
      module: 'STT_LISTENER',
      health: 'UP',
      activity: 'LISTENING',
      displayState: 'Listening',
      stale: false,
    },
    STT_WHISPER: {
      module: 'STT_WHISPER',
      health: 'UP',
      activity: 'IDLE',
      displayState: 'Idle',
      stale: false,
    },
    LLM: {
      module: 'LLM',
      health: 'UP',
      activity: 'IDLE',
      displayState: 'Idle',
      stale: false,
    },
    TTS_PIPER: {
      module: 'TTS_PIPER',
      health: 'UP',
      activity: 'IDLE',
      displayState: 'Silent',
      stale: false,
    },
    UI_GATEWAY: {
      module: 'UI_GATEWAY',
      health: 'DISABLED',
      activity: 'DISABLED',
      displayState: 'Planned',
      stale: false,
    },
    AVATAR: {
      module: 'AVATAR',
      health: 'DISABLED',
      activity: 'DISABLED',
      displayState: 'Placeholder',
      stale: false,
    },
  },
  lastUserUtterance: {
    text: 'How are you doing today?',
    occurredAt: new Date(Date.now() - 12_000).toISOString(),
    status: 'Accepted',
  },
  lastAssistantUtterance: {
    text: "I'm doing great. How can I help you today?",
    occurredAt: new Date(Date.now() - 5_000).toISOString(),
    status: 'Displayable',
  },
};
