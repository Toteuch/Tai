// SPDX-License-Identifier: GPL-3.0-only
import path from 'node:path';
import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
    strictPort: false,
    proxy: {
      '/tai-api': {
        target: process.env.VITE_TAI_ORCHESTRATOR_TARGET ?? 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (pathName) => pathName.replace(/^\/tai-api/, ''),
      },
    },
  },
});
