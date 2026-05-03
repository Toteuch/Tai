// SPDX-License-Identifier: GPL-3.0-only
/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_TAI_ORCHESTRATOR_BASE_URL?: string;
  readonly VITE_TAI_STOP_SPEAK_PATH?: string;
  readonly VITE_TAI_AVATAR_DIAGNOSTICS?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
