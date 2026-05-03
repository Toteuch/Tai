// SPDX-License-Identifier: GPL-3.0-only
import { useMemo } from 'react';
import { useInfiniteQuery } from '@tanstack/react-query';
import { History, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { ScrollArea } from '@/components/ui/scroll-area';
import { getConversationHistory } from '@/features/ui-state/api/taiUiApi';
import { ConversationTurn } from '@/features/ui-state/types';
import { formatDateTimeFromIso } from '@/lib/dateTime';
import { useLocalUiStore } from '@/store/localUiStore';

export function HistoryDialog() {
  const historyOpen = useLocalUiStore((state) => state.historyOpen);
  const setHistoryOpen = useLocalUiStore((state) => state.setHistoryOpen);

  const historyQuery = useInfiniteQuery({
    queryKey: ['conversation-history'],
    queryFn: ({ pageParam, signal }) =>
      getConversationHistory({ cursor: typeof pageParam === 'string' ? pageParam : null, limit: 20, signal }),
    enabled: historyOpen,
    initialPageParam: null as string | null,
    getNextPageParam: (lastPage) => lastPage.nextCursor ?? undefined,
  });

  const turns = useMemo(
    () => historyQuery.data?.pages.flatMap((page) => page.items) ?? [],
    [historyQuery.data],
  );

  return (
    <Dialog open={historyOpen} onOpenChange={setHistoryOpen}>
      <DialogContent className="grid h-[min(88vh,860px)] grid-rows-[auto_1fr_auto]">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-3">
            <History className="h-5 w-5 text-sky-300" />
            Conversation History
          </DialogTitle>
          <DialogDescription>
            Completed turns are loaded on demand from the orchestrator history endpoint.
          </DialogDescription>
        </DialogHeader>

        <ScrollArea className="min-h-0 rounded-2xl border border-white/10 bg-slate-950/55 p-4">
          {historyQuery.isLoading ? (
            <div className="grid h-full place-items-center py-24 text-slate-400">
              <Loader2 className="mb-3 h-6 w-6 animate-spin" />
              Loading history…
            </div>
          ) : historyQuery.isError ? (
            <div className="rounded-xl border border-orange-400/30 bg-orange-500/10 p-4 text-sm text-orange-100">
              Conversation history is unavailable right now.
            </div>
          ) : turns.length === 0 ? (
            <div className="rounded-xl bg-white/5 p-6 text-center text-sm text-slate-400">
              No completed conversation turns yet.
            </div>
          ) : (
            <div className="grid gap-4 pr-4">
              {turns.map((turn, index) => (
                <HistoryTurnCard key={turn.correlationId ?? index} turn={turn} />
              ))}
            </div>
          )}
        </ScrollArea>

        <div className="flex items-center justify-between gap-3">
          <p className="text-xs text-slate-500">Newest completed turns are shown first.</p>
          <Button
            variant="outline"
            disabled={!historyQuery.hasNextPage || historyQuery.isFetchingNextPage}
            onClick={() => historyQuery.fetchNextPage()}
          >
            {historyQuery.isFetchingNextPage ? (
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            ) : null}
            Load older turns
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}

function HistoryTurnCard({ turn }: { turn: ConversationTurn }) {
  return (
    <article className="rounded-2xl border border-white/10 bg-white/[0.035] p-4">
      <div className="grid gap-4 lg:grid-cols-2">
        <div className="rounded-xl bg-sky-500/10 p-3">
          <p className="mb-2 text-xs font-bold uppercase tracking-wide text-sky-300">User</p>
          <p className="text-sm leading-relaxed text-slate-100">{turn.userText || '—'}</p>
        </div>
        <div className="rounded-xl bg-violet-500/10 p-3">
          <p className="mb-2 text-xs font-bold uppercase tracking-wide text-violet-300">Assistant</p>
          <p className="text-sm leading-relaxed text-slate-100">{turn.assistantText || '—'}</p>
        </div>
      </div>
      <div className="mt-3 flex flex-wrap items-center gap-x-4 gap-y-2 text-xs text-slate-500">
        <span>Outcome: <strong className="text-slate-300">{turn.outcome ?? '—'}</strong></span>
        <span>Started: {formatDateTimeFromIso(turn.startedAt)}</span>
        <span>Completed: {formatDateTimeFromIso(turn.completedAt)}</span>
        {turn.correlationId ? <span>Correlation: {turn.correlationId}</span> : null}
      </div>
    </article>
  );
}
