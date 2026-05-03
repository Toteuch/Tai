// SPDX-License-Identifier: GPL-3.0-only
import { Expand, MonitorPlay, MoreVertical, Radio, RefreshCw } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import {
  DropdownMenu,
  DropdownMenuCheckboxItem,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Badge } from '@/components/ui/badge';
import { TaiUiState } from '@/features/ui-state/types';
import { labelForConversationStatus } from '@/features/ui-state/viewModel';
import { useLocalUiStore } from '@/store/localUiStore';
import { cn } from '@/lib/utils';

type Props = {
  uiState: TaiUiState;
};

const conversationGlow = {
  IDLE: 'from-slate-500/30 to-slate-800/30 border-slate-400/25',
  LISTENING: 'from-sky-500/30 to-cyan-800/25 border-sky-400/30',
  THINKING: 'from-blue-500/30 to-violet-800/25 border-blue-400/30',
  SPEAKING: 'from-violet-500/35 to-fuchsia-800/25 border-violet-300/40',
  ERROR: 'from-rose-500/35 to-orange-800/25 border-rose-300/40',
} as const;

export function AvatarPanel({ uiState }: Props) {
  const expandedAvatarOpen = useLocalUiStore((state) => state.expandedAvatarOpen);
  const setExpandedAvatarOpen = useLocalUiStore((state) => state.setExpandedAvatarOpen);
  const setSelectedModule = useLocalUiStore((state) => state.setSelectedModule);
  const diagnosticsOverlay = useLocalUiStore((state) => state.diagnosticsOverlay);
  const setDiagnosticsOverlay = useLocalUiStore((state) => state.setDiagnosticsOverlay);
  const setNotice = useLocalUiStore((state) => state.setNotice);

  return (
    <>
      <AvatarPanelFrame
        uiState={uiState}
        diagnosticsOverlay={diagnosticsOverlay}
        onExpand={() => setExpandedAvatarOpen(true)}
        onViewDetails={() => setSelectedModule('AVATAR')}
        onRefresh={() => setNotice('Avatar refresh is mocked until the avatar stream exists.')}
        onToggleDiagnostics={() => setDiagnosticsOverlay(!diagnosticsOverlay)}
      />

      <Dialog open={expandedAvatarOpen} onOpenChange={setExpandedAvatarOpen}>
        <DialogContent className="w-[min(96vw,1280px)] p-4">
          <DialogHeader className="px-2">
            <DialogTitle>Avatar View</DialogTitle>
            <DialogDescription>
              Enlarged placeholder render. This panel is ready to be replaced by a live avatar stream.
            </DialogDescription>
          </DialogHeader>
          <div className="h-[min(74vh,760px)] min-h-[520px] overflow-hidden rounded-2xl">
            <AvatarCanvas uiState={uiState} diagnosticsOverlay={diagnosticsOverlay} enlarged />
          </div>
        </DialogContent>
      </Dialog>
    </>
  );
}

type AvatarPanelFrameProps = Props & {
  diagnosticsOverlay: boolean;
  onExpand: () => void;
  onViewDetails: () => void;
  onRefresh: () => void;
  onToggleDiagnostics: () => void;
};

function AvatarPanelFrame({
  uiState,
  diagnosticsOverlay,
  onExpand,
  onViewDetails,
  onRefresh,
  onToggleDiagnostics,
}: AvatarPanelFrameProps) {
  return (
    <Card className="min-h-0 overflow-hidden">
      <CardHeader className="flex-row justify-between py-3">
        <div className="flex items-center gap-3">
          <MonitorPlay className="h-5 w-5 text-sky-300" />
          <CardTitle>Avatar Render</CardTitle>
          <Badge variant="disabled" className="hidden sm:inline-flex">Placeholder</Badge>
        </div>
        <div className="flex items-center gap-1">
          <Button variant="ghost" size="icon" onClick={onExpand} aria-label="Expand avatar render">
            <Expand className="h-5 w-5" />
          </Button>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" aria-label="Open avatar render actions">
                <MoreVertical className="h-5 w-5" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuLabel>Avatar render</DropdownMenuLabel>
              <DropdownMenuItem onClick={onViewDetails}>View avatar module details</DropdownMenuItem>
              <DropdownMenuItem onClick={onRefresh}>
                <RefreshCw className="mr-2 h-4 w-4" />
                Refresh render
              </DropdownMenuItem>
              <DropdownMenuCheckboxItem checked={diagnosticsOverlay} onCheckedChange={onToggleDiagnostics}>
                Toggle diagnostics overlay
              </DropdownMenuCheckboxItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem disabled>Open avatar stream</DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </CardHeader>
      <CardContent className="h-[calc(100%-3.55rem)] min-h-[360px] p-3 lg:min-h-0">
        <AvatarCanvas uiState={uiState} diagnosticsOverlay={diagnosticsOverlay} />
      </CardContent>
    </Card>
  );
}

function AvatarCanvas({
  uiState,
  diagnosticsOverlay,
  enlarged = false,
}: Props & { diagnosticsOverlay: boolean; enlarged?: boolean }) {
  const assistantText = uiState.lastAssistantUtterance?.text?.trim();
  const label = labelForConversationStatus(uiState.conversationStatus);

  return (
    <div className="relative h-full overflow-hidden rounded-2xl border border-white/10 bg-slate-950">
      <img
        src="/avatar-placeholder.png"
        alt="Tai avatar placeholder"
        className={cn(
          'h-full w-full object-cover object-center opacity-95 saturate-[1.08]',
          enlarged && 'object-[center_42%]',
        )}
      />
      <div className="absolute inset-0 bg-gradient-to-t from-black/65 via-transparent to-black/35" />
      <div className="absolute inset-x-0 top-4 flex justify-center px-4">
        <div
          className={cn(
            'flex min-w-[min(92%,34rem)] items-center justify-center gap-4 rounded-2xl border bg-gradient-to-r px-5 py-3 text-center shadow-glow backdrop-blur-xl',
            conversationGlow[uiState.conversationStatus],
          )}
        >
          <Radio className="h-5 w-5 text-violet-200" />
          <span className="text-xl font-black text-violet-100 drop-shadow sm:text-2xl">{label}</span>
          <Radio className="h-5 w-5 text-violet-200" />
        </div>
      </div>

      {diagnosticsOverlay ? (
        <div className="absolute right-4 top-24 rounded-xl border border-sky-400/30 bg-slate-950/75 p-3 text-xs text-sky-100 backdrop-blur-md">
          <p>Stream: Placeholder</p>
          <p>Sequence: {uiState.sequence}</p>
          <p>Status: {uiState.conversationStatus}</p>
        </div>
      ) : null}

      <div className="absolute inset-x-4 bottom-4 flex justify-center">
        <div className="max-w-[72rem] rounded-2xl border border-violet-300/40 bg-black/68 px-5 py-3 text-center text-lg font-semibold leading-snug text-white shadow-glow backdrop-blur-sm sm:text-2xl">
          {assistantText || 'No assistant reply yet'}
        </div>
      </div>
    </div>
  );
}
