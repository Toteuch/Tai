// SPDX-License-Identifier: GPL-3.0-only
import type { ComponentType } from 'react';
import {
  Bot,
  BrainCircuit,
  ChevronLeft,
  ChevronRight,
  Cpu,
  HeartPulse,
  Mic,
  Network,
  RadioTower,
  ServerCog,
  Volume2,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { ScrollArea } from '@/components/ui/scroll-area';
import { useModuleDetails } from '@/features/ui-state/hooks/useModuleDetails';
import { ModuleHealth, ModuleOverview, TaiModule } from '@/features/ui-state/types';
import { moduleLabel, moduleStateLabel, sortModules } from '@/features/ui-state/viewModel';
import { formatDateTimeFromIso } from '@/lib/dateTime';
import { cn } from '@/lib/utils';
import { useLocalUiStore } from '@/store/localUiStore';

type Props = {
  modules: Partial<Record<TaiModule, ModuleOverview>>;
};

const moduleIcons: Record<TaiModule, ComponentType<{ className?: string }>> = {
  SYSTEM: HeartPulse,
  ORCHESTRATOR: Network,
  STT_LISTENER: RadioTower,
  STT_WHISPER: Mic,
  LLM: BrainCircuit,
  TTS_PIPER: Volume2,
  UI_GATEWAY: ServerCog,
  AVATAR: Bot,
};

const healthVariant = {
  UP: 'healthy',
  DEGRADED: 'degraded',
  DOWN: 'down',
  DISABLED: 'disabled',
} as const;

function healthDotClass(health: ModuleHealth | undefined): string {
  switch (health) {
    case 'UP':
      return 'bg-emerald-400 shadow-[0_0_18px_rgba(52,211,153,0.85)]';
    case 'DEGRADED':
      return 'bg-orange-400 shadow-[0_0_18px_rgba(251,146,60,0.85)]';
    case 'DOWN':
      return 'bg-rose-400 shadow-[0_0_18px_rgba(251,113,133,0.85)]';
    case 'DISABLED':
    default:
      return 'bg-slate-500';
  }
}

export function ModulesPanel({ modules }: Props) {
  const selectedModule = useLocalUiStore((state) => state.selectedModule);
  const setSelectedModule = useLocalUiStore((state) => state.setSelectedModule);
  const detailsQuery = useModuleDetails(selectedModule);

  return (
    <Card className="min-h-0 overflow-hidden">
      <CardHeader className="py-3">
        <Cpu className="h-5 w-5 text-sky-300" />
        <CardTitle>{selectedModule ? moduleLabel(selectedModule) : 'System & Module Health'}</CardTitle>
      </CardHeader>

      {!selectedModule ? (
        <CardContent className="min-h-0 p-0">
          <ScrollArea className="h-full max-h-[36vh] lg:max-h-none">
            <div className="divide-y divide-white/10 px-4 pb-3">
              {sortModules(modules).map(([module, overview]) => {
                const Icon = moduleIcons[module];
                const health = overview?.health ?? (module === 'UI_GATEWAY' || module === 'AVATAR' ? 'DISABLED' : 'DOWN');
                return (
                  <button
                    key={module}
                    type="button"
                    onClick={() => setSelectedModule(module)}
                    className="grid w-full grid-cols-[1rem_2.25rem_minmax(9rem,1fr)_4.25rem_minmax(6rem,0.75fr)_1.5rem] items-center gap-2 py-3 text-left transition-colors hover:bg-white/[0.03]"
                  >
                    <span className={cn('h-3 w-3 rounded-full', healthDotClass(health))} />
                    <Icon className="h-5 w-5 text-slate-300" />
                    <span className="truncate text-sm font-medium text-slate-100">{moduleLabel(module)}</span>
                    <Badge variant={healthVariant[health]} className="justify-center rounded-lg px-2 py-1 uppercase">
                      {health}
                    </Badge>
                    <span className="truncate text-sm text-slate-300">{moduleStateLabel(module, overview)}</span>
                    <ChevronRight className="h-4 w-4 text-slate-500" />
                  </button>
                );
              })}
            </div>
          </ScrollArea>
        </CardContent>
      ) : (
        <CardContent className="min-h-0 overflow-hidden p-4">
          <Button variant="ghost" size="sm" className="mb-3" onClick={() => setSelectedModule(null)}>
            <ChevronLeft className="mr-2 h-4 w-4" />
            Back to overview
          </Button>

          {detailsQuery.isLoading ? (
            <p className="rounded-xl bg-white/5 p-4 text-sm text-slate-400">Loading module details…</p>
          ) : detailsQuery.isError ? (
            <p className="rounded-xl border border-orange-400/30 bg-orange-500/10 p-4 text-sm text-orange-100">
              Module details are unavailable. The live overview remains visible in the dashboard.
            </p>
          ) : (
            <div className="grid gap-3 text-sm">
              <DetailRow label="Health" value={detailsQuery.data?.health ?? modules[selectedModule]?.health ?? '—'} />
              <DetailRow label="State" value={detailsQuery.data?.displayState ?? detailsQuery.data?.state ?? moduleStateLabel(selectedModule, modules[selectedModule])} />
              <DetailRow label="Last checked" value={formatDateTimeFromIso(detailsQuery.data?.lastCheckedAt ?? detailsQuery.data?.lastHealthAt)} />
              <DetailRow label="Last activity" value={formatDateTimeFromIso(detailsQuery.data?.lastActivityAt)} />
              <DetailRow label="Correlation id" value={detailsQuery.data?.lastCorrelationId ?? '—'} />
              <DetailRow label="Last error" value={detailsQuery.data?.lastError ?? '—'} />
              <div className="rounded-xl bg-slate-950/50 p-3">
                <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-slate-500">Details</p>
                <pre className="max-h-36 overflow-auto whitespace-pre-wrap text-xs text-slate-300 scrollbar-thin-dark">
                  {JSON.stringify(detailsQuery.data?.details ?? detailsQuery.data?.healthDetails ?? {}, null, 2)}
                </pre>
              </div>
            </div>
          )}
        </CardContent>
      )}
    </Card>
  );
}

function DetailRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="grid grid-cols-[8rem_1fr] gap-3 rounded-xl bg-white/[0.035] px-3 py-2">
      <span className="text-slate-500">{label}</span>
      <span className="min-w-0 truncate text-slate-200">{value}</span>
    </div>
  );
}
