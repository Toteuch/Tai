// SPDX-License-Identifier: GPL-3.0-only
import {
  ConversationHistoryResponse,
  ConversationHistoryResponseSchema,
  ManualInputResponse,
  ManualInputResponseSchema,
  ModuleDetails,
  ModuleDetailsSchema,
  TaiModule,
  TaiUiState,
  TaiUiStateSchema,
} from '@/features/ui-state/types';
import { buildOrchestratorUrl, stopSpeakPath } from '@/features/ui-state/api/config';

async function readJson<T>(response: Response, parse: (value: unknown) => T): Promise<T> {
  if (!response.ok) {
    throw new Error(`HTTP ${response.status} ${response.statusText}`);
  }

  const payload = (await response.json()) as unknown;
  return parse(payload);
}

export async function getTaiUiState(signal?: AbortSignal): Promise<TaiUiState> {
  const response = await fetch(buildOrchestratorUrl('/ui/state'), {
    method: 'GET',
    signal,
    headers: {
      Accept: 'application/json',
    },
  });

  return readJson(response, (payload) => TaiUiStateSchema.parse(payload));
}

export function createTaiUiEventSource(): EventSource {
  return new EventSource(buildOrchestratorUrl('/ui/events'));
}

export async function getModuleDetails(module: TaiModule, signal?: AbortSignal): Promise<ModuleDetails> {
  const response = await fetch(buildOrchestratorUrl(`/ui/modules/${module}`), {
    method: 'GET',
    signal,
    headers: {
      Accept: 'application/json',
    },
  });

  return readJson(response, (payload) => ModuleDetailsSchema.parse(payload));
}

export async function getConversationHistory({
  cursor,
  limit = 20,
  signal,
}: {
  cursor?: string | null;
  limit?: number;
  signal?: AbortSignal;
}): Promise<ConversationHistoryResponse> {
  const params = new URLSearchParams({ limit: String(limit) });
  if (cursor) {
    params.set('cursor', cursor);
  }

  const response = await fetch(buildOrchestratorUrl(`/ui/history?${params.toString()}`), {
    method: 'GET',
    signal,
    headers: {
      Accept: 'application/json',
    },
  });

  const payload = (await readJson(response, (value) => value)) as unknown;
  if (Array.isArray(payload)) {
    return ConversationHistoryResponseSchema.parse({ items: payload });
  }
  return ConversationHistoryResponseSchema.parse(payload);
}

export async function sendManualInput(text: string): Promise<ManualInputResponse> {
  const response = await fetch(buildOrchestratorUrl('/ui/manual-input'), {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ text }),
  });

  return readJson(response, (payload) => ManualInputResponseSchema.parse(payload));
}

export function isStopSpeakConfigured(): boolean {
  return stopSpeakPath.length > 0;
}

export async function stopSpeak(correlationId: String): Promise<void> {
  if (!isStopSpeakConfigured()) {
    throw new Error('Stop Speak endpoint is not configured yet.');
  }

  const payload = {
    eventId: crypto.randomUUID(),
    occurredAt: new Date().toISOString(),
    correlationId: correlationId,
    source: 'UI',
  };

  const response = await fetch(buildOrchestratorUrl(stopSpeakPath), {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
      body: JSON.stringify(payload),
  });

  if (!response.ok) {
    throw new Error(`HTTP ${response.status} ${response.statusText}`);
  }
}
