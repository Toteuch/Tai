// SPDX-License-Identifier: GPL-3.0-only
import { Activity, Bell, CalendarDays, Clock3, Settings, UserCircle2 } from 'lucide-react';
import { AvatarPanel } from '@/features/avatar/AvatarPanel';
import { HistoryDialog } from '@/features/history/HistoryDialog';
import { ManualInputPanel } from '@/features/manual-input/ManualInputPanel';
import { ModulesPanel } from '@/features/modules/ModulesPanel';
import { ResourceUsagePanel } from '@/features/resources/ResourceUsagePanel';
import { LatestExchangesPanel } from '@/features/dashboard/LatestExchangesPanel';
import { StatusBadge } from '@/features/dashboard/StatusBadge';
import { useTaiUiState } from '@/features/ui-state/hooks/useTaiUiState';
import { useTaiUiStateStream } from '@/features/ui-state/hooks/useTaiUiStateStream';
import { computeSystemHealth } from '@/features/ui-state/viewModel';
import { formatClock, formatShortDate } from '@/lib/dateTime';
import { useCurrentTime } from '@/features/dashboard/useCurrentTime';
import { useLocalUiStore } from '@/store/localUiStore';
import { cn } from '@/lib/utils';

export function DashboardPage() {
  const { data: uiState, isError } = useTaiUiState();
  const now = useCurrentTime();
  const streamStatus = useLocalUiStore((state) => state.streamStatus);
  const notice = useLocalUiStore((state) => state.notice);
  const setNotice = useLocalUiStore((state) => state.setNotice);

  useTaiUiStateStream();

  const systemHealth = computeSystemHealth(uiState.modules);
  const disconnected = isError || streamStatus === 'disconnected' || streamStatus === 'reconnecting';

  return (
    <main className="min-h-screen overflow-hidden p-3 text-slate-100 sm:p-4">
      <div className="mx-auto flex h-[calc(100vh-1.5rem)] max-w-[1720px] flex-col gap-3 sm:h-[calc(100vh-2rem)]">
        <header className="flex h-[4.35rem] shrink-0 items-center justify-between rounded-2xl border border-white/10 bg-slate-950/60 px-4 shadow-panel backdrop-blur-xl">
          <div className="flex items-center gap-3">
            <div className="grid h-12 w-12 place-items-center rounded-full border border-violet-400/40 bg-gradient-to-br from-violet-600/35 to-sky-500/20 shadow-glow">
              <span className="text-2xl font-black text-transparent bg-clip-text bg-gradient-to-br from-violet-300 to-sky-300">
                T
              </span>
            </div>
            <div>
              <div className="flex items-baseline gap-3">
                <h1 className="text-3xl font-black tracking-tight text-transparent bg-clip-text bg-gradient-to-r from-violet-300 via-sky-300 to-violet-200">
                  Tai
                </h1>
                <span className="hidden text-xs font-semibold uppercase tracking-[0.55em] text-slate-400 sm:inline">
                  Control Center
                </span>
              </div>
              <p className="text-xs text-slate-500 sm:hidden">Control Center</p>
            </div>
          </div>

          <div className="hidden items-center gap-4 lg:flex">
            <StatusBadge health={systemHealth} disconnected={disconnected} />
            <div className="flex items-center gap-2 text-sm text-slate-300">
              <Clock3 className="h-4 w-4 text-slate-400" />
              {formatClock(now)}
            </div>
            <div className="flex items-center gap-2 text-sm text-slate-300">
              <CalendarDays className="h-4 w-4 text-slate-400" />
              {formatShortDate(now)}
            </div>
            <div className="h-8 w-px bg-white/10" />
            <Settings className="h-5 w-5 text-slate-400" aria-hidden="true" />
            <div className="relative">
              <Bell className="h-5 w-5 text-slate-400" aria-hidden="true" />
              <span className="absolute -right-2 -top-2 grid h-4 w-4 place-items-center rounded-full bg-violet-500 text-[10px] font-bold text-white">
                2
              </span>
            </div>
            <div className="flex items-center gap-2 rounded-xl bg-white/5 px-3 py-2 text-sm font-medium">
              <UserCircle2 className="h-5 w-5 text-slate-300" />
              Operator
            </div>
          </div>

          <div className="lg:hidden">
            <StatusBadge health={systemHealth} disconnected={disconnected} compact />
          </div>
        </header>

        {disconnected ? (
          <div className="shrink-0 rounded-xl border border-orange-400/30 bg-orange-500/10 px-4 py-2 text-sm text-orange-100">
            Showing the last known or fallback snapshot while the orchestrator stream reconnects.
          </div>
        ) : null}

        {notice ? (
          <div className="flex shrink-0 items-center justify-between gap-3 rounded-xl border border-sky-400/30 bg-sky-500/10 px-4 py-2 text-sm text-sky-100">
            <span>{notice}</span>
            <button className="text-xs font-semibold text-sky-200 hover:text-white" onClick={() => setNotice(null)}>
              Dismiss
            </button>
          </div>
        ) : null}

        <section
          className={cn(
            'grid min-h-0 flex-1 grid-cols-1 gap-3 overflow-y-auto lg:grid-cols-[minmax(360px,0.38fr)_1fr] lg:overflow-hidden',
          )}
        >
          <div className="grid min-h-0 gap-3 lg:grid-rows-[minmax(260px,0.98fr)_minmax(175px,0.55fr)_minmax(210px,0.62fr)] lg:overflow-hidden">
            <ModulesPanel modules={uiState.modules} />
            <LatestExchangesPanel uiState={uiState} />
            <ManualInputPanel conversationStatus={uiState.conversationStatus} correlationId={uiState.lastAssistantUtterance?.correlationId ?? crypto.randomUUID()} />
          </div>

          <div className="grid min-h-0 gap-3 lg:grid-rows-[minmax(420px,1fr)_minmax(210px,0.34fr)] lg:overflow-hidden">
            <AvatarPanel uiState={uiState} />
            <ResourceUsagePanel />
          </div>
        </section>
      </div>

      <HistoryDialog />
      <div className="sr-only" aria-live="polite">
        Stream status: {streamStatus}
      </div>
      <Activity className="hidden" aria-hidden="true" />
    </main>
  );
}
