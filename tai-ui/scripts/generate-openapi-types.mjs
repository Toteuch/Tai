import { mkdir } from 'node:fs/promises';
import { spawn } from 'node:child_process';

const baseUrl = process.env.VITE_TAI_ORCHESTRATOR_BASE_URL ?? 'http://localhost:8080';
const input = `${baseUrl.replace(/\/$/, '')}/v3/api-docs`;
const output = 'src/api/generated/orchestrator.ts';

await mkdir('src/api/generated', { recursive: true });

await new Promise((resolve, reject) => {
  const child = spawn('npx', ['openapi-typescript', input, '-o', output], {
    stdio: 'inherit',
    shell: process.platform === 'win32',
  });

  child.on('error', reject);
  child.on('exit', (code) => {
    if (code === 0) {
      resolve();
      return;
    }

    reject(new Error(`openapi-typescript exited with code ${code}`));
  });
}).catch((error) => {
  console.error(`Failed to generate OpenAPI types from ${input}.`);
  console.error('Make sure the orchestrator is running and exposes /v3/api-docs.');
  throw error;
});
