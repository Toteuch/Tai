// SPDX-License-Identifier: GPL-3.0-only
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import { ManualInputPanel } from '@/features/manual-input/ManualInputPanel';

function renderPanel() {
  const queryClient = new QueryClient();
  return render(
    <QueryClientProvider client={queryClient}>
      <ManualInputPanel conversationStatus="IDLE" />
    </QueryClientProvider>,
  );
}

describe('ManualInputPanel', () => {
  it('disables send for empty input and enables it once text is entered', async () => {
    const user = userEvent.setup();
    renderPanel();

    const sendButton = screen.getByRole('button', { name: /send/i });
    expect(sendButton).toBeDisabled();

    await user.type(screen.getByLabelText(/manual message/i), 'Hello Tai');
    expect(sendButton).toBeEnabled();
  });
});
