// SPDX-License-Identifier: GPL-3.0-only
import {
  ConversationStatus,
  ModuleHealth,
  ModuleOverview,
  RuntimeActivity,
  TaiModule,
  moduleOrder,
} from '@/features/ui-state/types';

export function labelForConversationStatus(status: ConversationStatus): string {
  switch (status) {
    case 'LISTENING':
      return 'Tai is listening';
    case 'THINKING':
      return 'Tai is thinking';
    case 'SPEAKING':
      return 'Tai is speaking';
    case 'ERROR':
      return 'Tai needs attention';
    case 'IDLE':
    default:
      return 'Tai is idle';
  }
}

export function healthLabel(health: ModuleHealth): string {
  switch (health) {
    case 'UP':
      return 'Healthy';
    case 'DEGRADED':
      return 'Degraded';
    case 'DOWN':
      return 'Down';
    case 'DISABLED':
    default:
      return 'Unavailable';
  }
}

export function moduleLabel(module: TaiModule): string {
  switch (module) {
    case 'SYSTEM':
      return 'System';
    case 'ORCHESTRATOR':
      return 'Orchestrator';
    case 'STT_LISTENER':
      return 'STT Listener';
    case 'STT_WHISPER':
      return 'STT Whisper';
    case 'LLM':
      return 'LLM';
    case 'TTS_PIPER':
      return 'TTS Piper';
    case 'UI_GATEWAY':
      return 'UI Gateway';
    case 'AVATAR':
      return 'Avatar';
    default:
      return module;
  }
}

export function runtimeActivityLabel(activity?: RuntimeActivity): string {
  switch (activity) {
    case 'DISABLED':
      return 'Disabled';
    case 'IDLE':
      return 'Idle';
    case 'LISTENING':
      return 'Listening';
    case 'CAPTURING':
      return 'Recording';
    case 'PROCESSING':
      return 'Processing';
    case 'GENERATING':
      return 'Generating';
    case 'SYNTHESIZING':
      return 'Synthesizing';
    case 'SPEAKING':
      return 'Speaking';
    case 'ERROR':
      return 'Error';
    case 'UNKNOWN':
    default:
      return 'Unknown';
  }
}

export function moduleStateLabel(module: TaiModule, overview?: ModuleOverview): string {
  if (!overview) {
    return module === 'UI_GATEWAY' || module === 'AVATAR' ? 'Planned' : 'Unknown';
  }

  return (
    overview.displayState ??
    overview.statusText ??
    overview.state ??
    runtimeActivityLabel(overview.runtimeActivity ?? overview.activity)
  );
}

export function sortModules(modules: Partial<Record<TaiModule, ModuleOverview>>): Array<[TaiModule, ModuleOverview | undefined]> {
  return moduleOrder.map((module) => [module, modules[module]]);
}

export function computeSystemHealth(modules: Partial<Record<TaiModule, ModuleOverview>>): ModuleHealth {
  const system = modules.SYSTEM?.health;
  if (system) {
    return system;
  }

  const healthValues = Object.values(modules).map((module) => module?.health).filter(Boolean);
  if (healthValues.some((health) => health === 'DOWN')) {
    return 'DOWN';
  }
  if (healthValues.some((health) => health === 'DEGRADED')) {
    return 'DEGRADED';
  }
  if (healthValues.some((health) => health === 'UP')) {
    return 'UP';
  }
  return 'DISABLED';
}
