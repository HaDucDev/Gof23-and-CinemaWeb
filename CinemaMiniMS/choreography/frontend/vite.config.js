import { defineConfig, loadEnv } from 'vite';
import { fileURLToPath } from 'node:url';

const envDir = fileURLToPath(new URL('.', import.meta.url));

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, envDir, 'BOOKING_API_TARGET');
  const target = env.BOOKING_API_TARGET || 'http://127.0.0.1:8081';
  const proxy = {
    '/api': { target, changeOrigin: false },
    '/ws': { target, ws: true, changeOrigin: false },
  };
  return {
    envDir,
    server: { host: '127.0.0.1', port: 5173, strictPort: true, proxy },
    preview: { host: '127.0.0.1', port: 5173, strictPort: true, proxy },
    build: { target: 'es2020' },
  };
});
