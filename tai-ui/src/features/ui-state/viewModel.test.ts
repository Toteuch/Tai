// SPDX-License-Identifier: GPL-3.0-only
import { describe, expect, it } from 'vitest';
import { labelForConversationStatus, moduleLabel, moduleStateLabel } from '@/features/ui-state/viewModel';

describe('Tai UI view model helpers', () => {
  it('maps conversation statuses to user-facing labels', () => {
    expect(labelForConversationStatus('LISTENING')).toBe('Tai is listening');
    expect(labelForConversationStatus('THINKING')).toBe('Tai is thinking');
    expect(labelForConversationStatus('SPEAKING')).toBe('Tai is speaking');
    expect(labelForConversationStatus('ERROR')).toBe('Tai needs attention');
  });

  it('maps modules to readable labels', () => {
    expect(moduleLabel('STT_LISTENER')).toBe('STT Listener');
    expect(moduleLabel('TTS_PIPER')).toBe('TTS Piper');
  });

  it('prefers explicit module display state over raw runtime activity', () => {
    expect(
      moduleStateLabel('LLM', {
        health: 'UP',
        activity: 'GENERATING',
        displayState: 'Generating reply',
        stale: false,
      }),
    ).toBe('Generating reply');
  });
});
