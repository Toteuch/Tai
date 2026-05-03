// SPDX-License-Identifier: GPL-3.0-only
import { Bot, History, MessageSquareText, UserRound } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { TaiUiState } from '@/features/ui-state/types';
import { labelForConversationStatus } from '@/features/ui-state/viewModel';
import { formatTimeFromIso } from '@/lib/dateTime';
import { useLocalUiStore } from '@/store/localUiStore';

type Props = {
  uiState: TaiUiState;
};

export function LatestExchangesPanel({ uiState }: Props) {
  const setHistoryOpen = useLocalUiStore((state) => state.setHistoryOpen);
  const userText = uiState.lastUserUtterance?.text?.trim();
  const assistantText = uiState.lastAssistantUtterance?.text?.trim();

  return (
    <Card className="min-h-0 overflow-hidden">
      <CardHeader className="flex-row justify-between py-3">
        <div className="flex items-center gap-3">
          <MessageSquareText className="h-5 w-5 text-sky-300" />
          <CardTitle>Latest Exchanges</CardTitle>
        </div>
        <Button variant="outline" size="sm" onClick={() => setHistoryOpen(true)}>
          <History className="mr-2 h-4 w-4" />
          History
        </Button>
      </CardHeader>
      <CardContent className="grid min-h-0 gap-4 overflow-hidden p-4">
        <article className="grid grid-cols-[2.75rem_1fr] gap-3">
          <div className="grid h-10 w-10 place-items-center rounded-full bg-sky-500/20 text-sky-200">
            <UserRound className="h-5 w-5" />
          </div>
          <div className="min-w-0 border-l-2 border-sky-400/70 pl-4">
            <div className="flex items-center gap-4 text-xs font-semibold uppercase tracking-wide">
              <span className="text-sky-300">User</span>
              <span className="text-slate-500">{formatTimeFromIso(uiState.lastUserUtterance?.occurredAt ?? uiState.lastUserUtterance?.createdAt ?? uiState.lastUserUtterance?.timestamp)}</span>
            </div>
            <p className="mt-1 line-clamp-3 text-base text-slate-100">
              {userText || 'No user message yet'}
            </p>
          </div>
        </article>

        <article className="grid grid-cols-[2.75rem_1fr] gap-3">
          <div className="grid h-10 w-10 place-items-center rounded-full bg-violet-500/20 text-violet-200">
            <Bot className="h-5 w-5" />
          </div>
          <div className="min-w-0 border-l-2 border-violet-400/70 pl-4">
            <div className="flex items-center gap-4 text-xs font-semibold uppercase tracking-wide">
              <span className="text-violet-300">Assistant</span>
              <span className="text-slate-500">{labelForConversationStatus(uiState.conversationStatus)}</span>
            </div>
            <p className="mt-1 line-clamp-2 text-sm text-slate-300">
              {assistantText ? 'Displayed as avatar subtitle' : 'No assistant reply yet'}
            </p>
          </div>
        </article>
      </CardContent>
    </Card>
  );
}
