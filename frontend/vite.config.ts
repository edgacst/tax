import react from '@vitejs/plugin-react'
import type { Plugin, PreviewServer, ViteDevServer } from 'vite'
import { defineConfig } from 'vite'

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

/**
 * `/dlrp` 같은 오타·직접 URL 입력 시에도 React 앱(index.html)이 뜨도록 함.
 * (정적 파일·API·Vite 내부 경로는 그대로 둠)
 */
function spaFallback(): Plugin {
  const skipPrefix = ['/api', '/v3', '/swagger-ui', '/webjars', '/@', '/src', '/node_modules']

  function shouldSpaFallback(pathname: string): boolean {
    if (!pathname || pathname === '/') {
      return false
    }
    if (skipPrefix.some((p) => pathname.startsWith(p))) {
      return false
    }
    const last = pathname.split('/').filter(Boolean).pop() ?? ''
    if (last.includes('.')) {
      return false
    }
    return true
  }

  function mount(server: ViteDevServer | PreviewServer) {
    server.middlewares.use((req, _res, next) => {
      if (req.method !== 'GET') {
        next()
        return
      }
      const pathname = (req.url ?? '').split('?')[0] ?? ''
      if (shouldSpaFallback(pathname)) {
        req.url = '/index.html'
      }
      next()
    })
  }

  return {
    name: 'taxflow-spa-fallback',
    configureServer(server) {
      mount(server)
    },
    configurePreviewServer(server) {
      mount(server)
    },
  }
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), spaFallback()],
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
