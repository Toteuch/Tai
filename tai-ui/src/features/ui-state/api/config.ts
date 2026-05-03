// SPDX-License-Identifier: GPL-3.0-only
export const orchestratorBaseUrl = (
  import.meta.env.VITE_TAI_ORCHESTRATOR_BASE_URL ?? '/tai-api'
).replace(/\/$/, '');

export const stopSpeakPath = (import.meta.env.VITE_TAI_STOP_SPEAK_PATH ?? '').trim();

export function buildOrchestratorUrl(path: string): string {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  return `${orchestratorBaseUrl}${normalizedPath}`;
}
