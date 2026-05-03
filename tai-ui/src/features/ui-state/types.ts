// SPDX-License-Identifier: GPL-3.0-only
import { z } from 'zod';

export const ConversationStatusSchema = z.enum(['IDLE', 'LISTENING', 'THINKING', 'SPEAKING', 'ERROR']);
export type ConversationStatus = z.infer<typeof ConversationStatusSchema>;

export const ModuleHealthSchema = z.enum(['UP', 'DEGRADED', 'DOWN', 'DISABLED']);
export type ModuleHealth = z.infer<typeof ModuleHealthSchema>;

export const RuntimeActivitySchema = z.enum([
  'UNKNOWN',
  'DISABLED',
  'IDLE',
  'LISTENING',
  'CAPTURING',
  'PROCESSING',
  'GENERATING',
  'SYNTHESIZING',
  'SPEAKING',
  'ERROR',
]);
export type RuntimeActivity = z.infer<typeof RuntimeActivitySchema>;

export const TaiModuleSchema = z.enum([
  'SYSTEM',
  'ORCHESTRATOR',
  'STT_LISTENER',
  'STT_WHISPER',
  'LLM',
  'TTS_PIPER',
  'UI_GATEWAY',
  'AVATAR',
]);
export type TaiModule = z.infer<typeof TaiModuleSchema>;

export const UtteranceSchema = z
  .object({
    text: z.string().optional().nullable(),
    occurredAt: z.string().optional().nullable(),
    createdAt: z.string().optional().nullable(),
    timestamp: z.string().optional().nullable(),
    status: z.string().optional().nullable(),
    correlationId: z.string().optional().nullable(),
  })
  .passthrough();
export type Utterance = z.infer<typeof UtteranceSchema>;

export const ModuleOverviewSchema = z
  .object({
    module: TaiModuleSchema.optional(),
    name: z.string().optional().nullable(),
    displayName: z.string().optional().nullable(),
    health: ModuleHealthSchema.optional().default('DISABLED'),
    activity: RuntimeActivitySchema.optional().default('UNKNOWN'),
    runtimeActivity: RuntimeActivitySchema.optional(),
    state: z.string().optional().nullable(),
    displayState: z.string().optional().nullable(),
    statusText: z.string().optional().nullable(),
    stale: z.boolean().optional().default(false),
    lastHealthAt: z.string().optional().nullable(),
    lastActivityAt: z.string().optional().nullable(),
    lastCorrelationId: z.string().optional().nullable(),
    lastError: z.string().optional().nullable(),
  })
  .passthrough();
export type ModuleOverview = z.infer<typeof ModuleOverviewSchema>;

export const TaiUiStateSchema = z
  .object({
    schemaVersion: z.string().optional().default('2.0'),
    sequence: z.number().optional().default(0),
    generatedAt: z.string().optional().default(() => new Date().toISOString()),
    conversationStatus: ConversationStatusSchema.optional().default('IDLE'),
    modules: z.record(TaiModuleSchema, ModuleOverviewSchema).optional().default({}),
    lastUserUtterance: UtteranceSchema.optional().nullable(),
    lastAssistantUtterance: UtteranceSchema.optional().nullable(),
  })
  .passthrough();
export type TaiUiState = z.infer<typeof TaiUiStateSchema>;

export const ModuleDetailsSchema = z
  .object({
    module: TaiModuleSchema.optional(),
    health: ModuleHealthSchema.optional(),
    activity: RuntimeActivitySchema.optional(),
    runtimeActivity: RuntimeActivitySchema.optional(),
    state: z.string().optional().nullable(),
    displayState: z.string().optional().nullable(),
    lastHealthAt: z.string().optional().nullable(),
    lastCheckedAt: z.string().optional().nullable(),
    lastActivityAt: z.string().optional().nullable(),
    lastCorrelationId: z.string().optional().nullable(),
    lastDurationMs: z.number().optional().nullable(),
    lastError: z.string().optional().nullable(),
    details: z.record(z.unknown()).optional().nullable(),
    healthDetails: z.record(z.unknown()).optional().nullable(),
  })
  .passthrough();
export type ModuleDetails = z.infer<typeof ModuleDetailsSchema>;

export const ConversationTurnSchema = z
  .object({
    correlationId: z.string().optional().nullable(),
    userText: z.string().optional().nullable(),
    assistantText: z.string().optional().nullable(),
    outcome: z.string().optional().nullable(),
    startedAt: z.string().optional().nullable(),
    completedAt: z.string().optional().nullable(),
  })
  .passthrough();
export type ConversationTurn = z.infer<typeof ConversationTurnSchema>;

export const ConversationHistoryResponseSchema = z
  .object({
    items: z.array(ConversationTurnSchema).optional().default([]),
    nextCursor: z.string().optional().nullable(),
    cursor: z.string().optional().nullable(),
  })
  .passthrough()
  .transform((value) => ({
    ...value,
    nextCursor:
      value.nextCursor ??
      (value.items.length > 0 ? value.items[value.items.length - 1]?.correlationId ?? null : null),
  }));
export type ConversationHistoryResponse = z.infer<typeof ConversationHistoryResponseSchema>;

export const ManualInputResponseSchema = z
  .object({
    accepted: z.boolean(),
    correlationId: z.string().optional().nullable(),
    acceptedAt: z.string().optional().nullable(),
  })
  .passthrough();
export type ManualInputResponse = z.infer<typeof ManualInputResponseSchema>;

export const moduleOrder: TaiModule[] = [
  'SYSTEM',
  'ORCHESTRATOR',
  'STT_LISTENER',
  'STT_WHISPER',
  'LLM',
  'TTS_PIPER',
  'UI_GATEWAY',
  'AVATAR',
];
