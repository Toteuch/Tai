// SPDX-License-Identifier: GPL-3.0-only
import * as React from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import { cn } from '@/lib/utils';

const badgeVariants = cva(
  'inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors',
  {
    variants: {
      variant: {
        default: 'border-transparent bg-primary text-primary-foreground',
        secondary: 'border-transparent bg-secondary text-secondary-foreground',
        outline: 'border-white/15 text-slate-200',
        healthy: 'border-emerald-400/30 bg-emerald-500/15 text-emerald-200',
        degraded: 'border-orange-400/30 bg-orange-500/15 text-orange-200',
        down: 'border-rose-400/30 bg-rose-500/15 text-rose-200',
        disabled: 'border-slate-400/20 bg-slate-500/10 text-slate-300',
        info: 'border-sky-400/30 bg-sky-500/15 text-sky-200',
        speaking: 'border-violet-400/30 bg-violet-500/15 text-violet-100',
      },
    },
    defaultVariants: {
      variant: 'default',
    },
  },
);

export interface BadgeProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof badgeVariants> {}

function Badge({ className, variant, ...props }: BadgeProps) {
  return <div className={cn(badgeVariants({ variant }), className)} {...props} />;
}

export { Badge, badgeVariants };
