// SPDX-License-Identifier: GPL-3.0-only
import { Keyboard, Send, Square } from 'lucide-react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Textarea } from '@/components/ui/textarea';
import {
  isStopSpeakConfigured,
  sendManualInput,
  stopSpeak,
} from '@/features/ui-state/api/taiUiApi';
import { taiUiStateQueryKey } from '@/features/ui-state/hooks/useTaiUiState';
import { ConversationStatus } from '@/features/ui-state/types';
import { useLocalUiStore } from '@/store/localUiStore';
import { cn } from '@/lib/utils';

const MANUAL_INPUT_LIMIT = 2000;

type Props = {
  conversationStatus: ConversationStatus;
  correlationId: string;
};

export function ManualInputPanel({ conversationStatus, correlationId }: Props) {
  const queryClient = useQueryClient();
  const inputDraft = useLocalUiStore((state) => state.inputDraft);
  const setInputDraft = useLocalUiStore((state) => state.setInputDraft);
  const setNotice = useLocalUiStore((state) => state.setNotice);

  const trimmed = inputDraft.trim();
  const remaining = MANUAL_INPUT_LIMIT - inputDraft.length;
  const inputValid = trimmed.length > 0 && inputDraft.length <= MANUAL_INPUT_LIMIT;

  const manualInputMutation = useMutation({
    mutationFn: sendManualInput,
    onSuccess: (response) => {
      if (response.accepted) {
        setInputDraft('');
        setNotice(`Manual input accepted${response.correlationId ? ` (${response.correlationId})` : ''}.`);
        void queryClient.invalidateQueries({ queryKey: taiUiStateQueryKey });
      }
    },
    onError: (error) => {
      setNotice(error instanceof Error ? error.message : 'Manual input failed.');
    },
  });

  const stopSpeakMutation = useMutation({
    mutationFn: stopSpeak,
    onSuccess: () => {
      setNotice('Stop Speak request sent.');
      void queryClient.invalidateQueries({ queryKey: taiUiStateQueryKey });
    },
    onError: (error) => {
      setNotice(error instanceof Error ? error.message : 'Stop Speak request failed.');
    },
  });

  const stopConfigured = isStopSpeakConfigured();
  const stopEnabled = conversationStatus === 'SPEAKING' && stopConfigured && !stopSpeakMutation.isPending;

  return (
    <Card className="min-h-0 overflow-hidden">
      <CardHeader className="py-3">
        <Keyboard className="h-5 w-5 text-sky-300" />
        <CardTitle>Manual Input</CardTitle>
      </CardHeader>
      <CardContent className="grid min-h-0 gap-3 p-4">
        <Textarea
          value={inputDraft}
          maxLength={MANUAL_INPUT_LIMIT + 1}
          onChange={(event) => setInputDraft(event.target.value)}
          placeholder="Type a message to Tai…"
          aria-label="Manual message to Tai"
          className="min-h-[90px] resize-none lg:min-h-0"
        />
        <div className="flex items-center justify-between gap-3">
          <span
            className={cn(
              'text-xs text-slate-500',
              remaining < 0 && 'font-semibold text-rose-300',
              remaining >= 0 && remaining < 160 && 'text-orange-300',
            )}
          >
            {inputDraft.length}/{MANUAL_INPUT_LIMIT}
          </span>
          <span className="text-xs text-slate-500">Enter text only; STT is bypassed.</span>
        </div>
        <div className="grid grid-cols-2 gap-3">
          <Button
            type="button"
            disabled={!inputValid || manualInputMutation.isPending}
            onClick={() => manualInputMutation.mutate(trimmed)}
          >
            <Send className="mr-2 h-4 w-4" />
            Send
          </Button>
          <Button
            type="button"
            variant={conversationStatus === 'SPEAKING' ? 'destructive' : 'outline'}
            disabled={!stopEnabled}
            title={stopConfigured ? 'Stop current assistant speech' : 'Stop Speak endpoint is not exposed yet'}
            onClick={() => stopSpeakMutation.mutate(correlationId)}
          >
            <Square className="mr-2 h-4 w-4" />
            Stop Speak
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
