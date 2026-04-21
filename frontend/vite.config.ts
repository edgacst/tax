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
    // 127.0.0.1 / LAN 모두에서 접속 가능 (Windows에서 localhost vs 127 차이 완화)
    host: true,
    port: 5173,
    // 5173 사용 중이면 다음 포트로 뜸 — strictPort true 면 즉시 종료되어 "안 뜨는" 것처럼 보일 수 있음
    strictPort: false,
    proxy: backendProxy,
  },
  preview: {
    host: true,
    port: 4173,
    strictPort: false,
    proxy: backendProxy,
  },
})
