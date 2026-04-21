import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

/** dev(`vite`) / preview(`vite preview`) 둘 다 백엔드(8080)로 넘김 — preview는 server.proxy만으로는 동작하지 않음 */
const backendProxy = {
  '/api': {
    target: 'http://127.0.0.1:8080',
    changeOrigin: true,
  },
  '/v3': {
    target: 'http://127.0.0.1:8080',
    changeOrigin: true,
  },
  '/swagger-ui': {
    target: 'http://127.0.0.1:8080',
    changeOrigin: true,
  },
  '/webjars': {
    target: 'http://127.0.0.1:8080',
    changeOrigin: true,
  },
} satisfies Record<string, { target: string; changeOrigin: boolean }>

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true,
    proxy: backendProxy,
  },
  preview: {
    port: 4173,
    strictPort: true,
    proxy: backendProxy,
  },
})
