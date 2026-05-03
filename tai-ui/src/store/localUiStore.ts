// SPDX-License-Identifier: GPL-3.0-only
import { create } from 'zustand';
import { TaiModule } from '@/features/ui-state/types';

export type StreamConnectionStatus = 'connecting' | 'connected' | 'reconnecting' | 'disconnected';
export type ResourceKind = 'CPU' | 'GPU' | 'RAM';

type LocalUiState = {
  inputDraft: string;
  setInputDraft: (value: string) => void;
  historyOpen: boolean;
  setHistoryOpen: (value: boolean) => void;
  expandedAvatarOpen: boolean;
  setExpandedAvatarOpen: (value: boolean) => void;
  selectedResource: ResourceKind;
  setSelectedResource: (value: ResourceKind) => void;
  streamStatus: StreamConnectionStatus;
  setStreamStatus: (value: StreamConnectionStatus) => void;
  selectedModule: TaiModule | null;
  setSelectedModule: (value: TaiModule | null) => void;
  diagnosticsOverlay: boolean;
  setDiagnosticsOverlay: (value: boolean) => void;
  notice: string | null;
  setNotice: (value: string | null) => void;
};

export const useLocalUiStore = create<LocalUiState>((set) => ({
  inputDraft: '',
  setInputDraft: (value) => set({ inputDraft: value }),
  historyOpen: false,
  setHistoryOpen: (value) => set({ historyOpen: value }),
  expandedAvatarOpen: false,
  setExpandedAvatarOpen: (value) => set({ expandedAvatarOpen: value }),
  selectedResource: 'CPU',
  setSelectedResource: (value) => set({ selectedResource: value }),
  streamStatus: 'connecting',
  setStreamStatus: (value) => set({ streamStatus: value }),
  selectedModule: null,
  setSelectedModule: (value) => set({ selectedModule: value }),
  diagnosticsOverlay: import.meta.env.VITE_TAI_AVATAR_DIAGNOSTICS === 'true',
  setDiagnosticsOverlay: (value) => set({ diagnosticsOverlay: value }),
  notice: null,
  setNotice: (value) => set({ notice: value }),
}));
