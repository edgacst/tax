import { useCallback, useState } from 'react'
import { fetchPing } from '../lib/api'
import { useUiStore } from '../store/uiStore'

export function HomePage() {
  const [loading, setLoading] = useState(false)
  const { lastPingJson, lastHttpStatus, lastError, setPingResult } = useUiStore()

  const onPing = useCallback(async () => {
    setLoading(true)
    try {
      const data = await fetchPing()
      const json = JSON.stringify(data, null, 2)
      setPingResult({ json, status: 200, error: null })
    } catch (e) {
      const msg = e instanceof Error ? e.message : String(e)
      setPingResult({
        json: msg,
        status: 0,
        error: '백엔드(8080)가 켜져 있는지, `docker compose up -d` 후 `bootRun` 했는지 확인하세요.',
      })
    } finally {
      setLoading(false)
    }
  }, [setPingResult])

  return (
    <div className="space-y-6">
      <div>
        <h1 className="mb-2 text-2xl font-bold tracking-tight text-white">시작하기</h1>
        <p className="max-w-2xl text-sm leading-relaxed text-slate-400">
          위 메뉴로 <strong className="text-slate-300">세금계산서</strong>·
          <strong className="text-slate-300">설정</strong> 화면으로 이동할 수 있습니다. 이 페이지는 백엔드 연결을
          확인하는 <strong className="text-slate-300">개발용 홈</strong>입니다. API는 Vite 프록시로{' '}
          <code className="rounded bg-slate-800 px-1 py-0.5 text-xs">8080</code> 으로 전달됩니다.
        </p>
      </div>

      <div className="max-w-md rounded-2xl border border-surface-border bg-surface-card p-5 shadow-2xl shadow-black/40">
        <h2 className="mb-3 text-sm font-semibold text-slate-300">백엔드 연결 테스트</h2>
        <div className="mb-4 flex flex-wrap gap-2">
          <button
            type="button"
            onClick={onPing}
            disabled={loading}
            className="rounded-xl bg-gradient-to-br from-blue-500 to-blue-700 px-4 py-2.5 text-sm font-semibold text-white shadow-md transition hover:brightness-110 disabled:opacity-60"
          >
            {loading ? '호출 중…' : 'Ping 호출'}
          </button>
          <a
            className="inline-flex items-center rounded-xl border border-surface-border px-4 py-2.5 text-sm font-semibold text-slate-300 transition hover:border-slate-500 hover:text-white"
            href="/swagger-ui/index.html"
            target="_blank"
            rel="noreferrer"
          >
            Swagger UI
          </a>
        </div>

        <pre className="min-h-[5.5rem] overflow-x-auto rounded-xl border border-slate-800 bg-[#0d1117] p-4 font-mono text-xs leading-relaxed text-slate-300">
          {lastPingJson ?? '응답이 여기에 표시됩니다.'}
        </pre>

        {lastHttpStatus === 200 && !lastError && lastPingJson && (
          <p className="mt-3 text-xs font-medium text-emerald-400">HTTP 200 — 정상</p>
        )}
        {lastError && <p className="mt-3 text-xs font-medium text-rose-400">{lastError}</p>}
      </div>
    </div>
  )
}
