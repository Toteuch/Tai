// SPDX-License-Identifier: GPL-3.0-only
import { Circle } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { ModuleHealth } from '@/features/ui-state/types';
import { healthLabel } from '@/features/ui-state/viewModel';
import { cn } from '@/lib/utils';

const healthVariant = {
  UP: 'healthy',
  DEGRADED: 'degraded',
  DOWN: 'down',
  DISABLED: 'disabled',
} as const;

type Props = {
  health: ModuleHealth;
  disconnected?: boolean;
  compact?: boolean;
};

export function StatusBadge({ health, disconnected, compact }: Props) {
  const displayHealth = disconnected ? 'DEGRADED' : health;
  return (
    <Badge
      variant={healthVariant[displayHealth]}
      className={cn('gap-2 rounded-xl px-4 py-2 text-sm', compact && 'px-3 text-xs')}
    >
      <Circle className="h-2.5 w-2.5 fill-current" />
      <span className="text-slate-200">System overall:</span>
      <span className="uppercase">{disconnected ? 'Reconnecting' : healthLabel(health)}</span>
    </Badge>
  );
}
