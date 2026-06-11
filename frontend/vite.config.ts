import react from '@vitejs/plugin-react'
import type { Plugin, PreviewServer, ViteDevServer } from 'vite'
import { defineConfig, loadEnv } from 'vite'

/**
 * `/dlrp` 같은 오타·직접 URL 입력 시에도 React 앱(index.html)이 뜨도록 함.
 * (정적 파일·API·Vite 내부 경로는 그대로 둠)
 */
function spaFallback(): Plugin {
  const skipPrefix = ['/api', '/v3', '/swagger-ui', '/webjars', '/@', '/src', '/node_modules', '/assets']

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
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const backendPort = env.VITE_BACKEND_PORT || '8080'
  const backendTarget = `http://127.0.0.1:${backendPort}`

  /** dev(`vite`) / preview(`vite preview`) 둘 다 백엔드로 넘김 */
  const backendProxy = {
    '/api': { target: backendTarget, changeOrigin: true },
    '/v3': { target: backendTarget, changeOrigin: true },
    '/swagger-ui': { target: backendTarget, changeOrigin: true },
    '/webjars': { target: backendTarget, changeOrigin: true },
  }

  return {
    plugins: [react(), spaFallback()],
    server: {
      host: true,
      port: 5173,
      strictPort: false,
      proxy: backendProxy,
    },
    preview: {
      host: true,
      port: 4173,
      strictPort: false,
      proxy: backendProxy,
    },
  }
})
